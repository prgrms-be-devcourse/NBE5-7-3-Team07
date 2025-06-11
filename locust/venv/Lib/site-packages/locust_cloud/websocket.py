import logging
import sys
import threading
import urllib.parse

import socketio
import socketio.exceptions

logger = logging.getLogger(__name__)


class SessionMismatchError(Exception):
    pass


class WebsocketTimeout(Exception):
    pass


class Websocket:
    def __init__(self) -> None:
        """
        This class was created to encapsulate all the logic involved in the websocket implementation.
        The behaviour of the socketio client once a connection has been established
        is to try to reconnect forever if the connection is lost.
        The way this can be canceled is by setting the _reconnect_abort (threading.Event) on the client
        in which case it will simply proceed with shutting down without giving any indication of an error.
        This class handles timeouts for connection attempts as well as some logic around when the
        socket can be shut down. See descriptions on the methods for further details.
        """
        self.__shutdown_allowed = threading.Event()
        self.__timeout_on_disconnect = True
        self.initial_connect_timeout = 120
        self.reconnect_timeout = 10
        self.wait_timeout = 0
        self.exception: None | Exception = None

        self.sio = socketio.Client(handle_sigint=False)
        self.sio._reconnect_abort = threading.Event()
        # The _reconnect_abort value on the socketio client will be populated with a newly created threading.Event if it's not already set.
        # There is no way to set this by passing it in the constructor.
        # This event is the only way to interupt the retry logic when the connection is attempted.

        self.sio.on("connect", self.__on_connect)
        self.sio.on("disconnect", self.__on_disconnect)
        self.sio.on("connect_error", self.__on_connect_error)
        self.sio.on("events", self.__on_events)

        self.__processed_events: set[int] = set()

    def __set_connection_timeout(self, timeout) -> None:
        """
        Start a threading.Timer that will set the threading.Event on the socketio client
        that aborts any further attempts to reconnect, sets an exception on the websocket
        that will be raised from the wait method and the threading.Event __shutdown_allowed
        on the websocket that tells the wait method that it should stop blocking.
        """

        def _timeout():
            logger.debug(f"Websocket connection timed out after {timeout} seconds")
            self.sio._reconnect_abort.set()
            self.exception = WebsocketTimeout("Timed out connecting to locust master")
            self.__shutdown_allowed.set()

        self.__connect_timeout_timer = threading.Timer(timeout, _timeout)
        self.__connect_timeout_timer.daemon = True
        # logger.debug(f"Setting websocket connection timeout to {timeout} seconds")
        self.__connect_timeout_timer.start()

    def connect(self, url, *, auth) -> None:
        """
        Send along retry=True when initiating the socketio client connection
        to make it use it's builtin logic for retrying failed connections that
        is usually used for reconnections. This will retry forever.
        When connecting start a timer to trigger disabling the retry logic and
        raise a WebsocketTimeout exception.
        """
        ws_connection_info = urllib.parse.urlparse(url)
        self.__set_connection_timeout(self.initial_connect_timeout)
        try:
            self.sio.connect(
                f"{ws_connection_info.scheme}://{ws_connection_info.netloc}",
                auth=auth,
                retry=True,
                **{"socketio_path": ws_connection_info.path} if ws_connection_info.path else {},
            )
        except socketio.exceptions.ConnectionError:
            if self.exception:
                raise self.exception

            raise

    def shutdown(self) -> None:
        """
        When shutting down the socketio client a disconnect event will fire.
        Before doing so disable the behaviour of starting a threading.Timer
        to handle timeouts on attempts to reconnect since no further such attempts
        will be made.
        If such a timer is already running, cancel it since the client is being shutdown.
        """
        self.__timeout_on_disconnect = False
        if hasattr(self, "__connect_timeout_timer"):
            self.__connect_timeout_timer.cancel()
        self.sio.shutdown()

    def wait(self, timeout=False) -> bool:
        """
        Block until the threading.Event __shutdown_allowed is set, with a timeout if indicated.
        If an exception has been set on the websocket (from a connection timeout timer or the
        __on_connect_error method), raise it.
        """
        timeout = self.wait_timeout if timeout else None
        if timeout:  # not worth even debug logging if we dont have a timeout
            logger.debug(f"Waiting for shutdown for {str(timeout) + 's' if timeout else 'ever'}")
        res = self.__shutdown_allowed.wait(timeout)
        if self.exception:
            raise self.exception
        return res

    def __on_connect(self) -> None:
        """
        This gets events whenever a connection is successfully established.
        When this happens, cancel the running threading.Timer that would
        abort reconnect attempts and raise a WebsocketTimeout exception.
        The wait_timeout is originally set to zero when creating the websocket
        but once a connection has been established this is raised to ensure
        that the server is given the chance to send all the logs and an
        official shutdown event.
        """
        self.__connect_timeout_timer.cancel()
        self.wait_timeout = 90
        logger.debug("Websocket connected")

    def __on_disconnect(self) -> None:
        """
        This gets events whenever a connection is lost.
        The socketio client will try to reconnect forever so,
        unless the behaviour has been disabled, a threading.Timer
        is started that will abort reconnect attempts and raise a
        WebsocketTimeout exception.
        """
        if self.__timeout_on_disconnect:
            self.__set_connection_timeout(self.reconnect_timeout)
        logger.debug("Websocket disconnected")

    def __on_events(self, data):
        """
        This gets events explicitly sent by the websocket server.
        This will either be messages to print on stdout/stderr or
        an indication that the CLI can shut down in which case the
        threading.Event __shutdown_allowed gets set on the websocket
        that tells the wait method that it should stop blocking.
        """
        shutdown = False
        shutdown_message = ""

        if data["id"] in self.__processed_events:
            logger.debug(f"Got duplicate data on websocket, id {data['id']}")
            return

        self.__processed_events.add(data["id"])

        for event in data["events"]:
            type = event["type"]

            if type == "shutdown":
                shutdown = True
                shutdown_message = event["message"]
            elif type == "stdout":
                sys.stdout.write(event["message"])
            elif type == "stderr":
                sys.stderr.write(event["message"])
            else:
                raise Exception("Unexpected event type")

        if shutdown:
            logger.debug("Got shutdown from locust master")
            self.__connect_timeout_timer.cancel()  # I dont know exactly why/if this is necessary but we had an issue in status-checker once
            if shutdown_message:
                print(shutdown_message)

            self.__shutdown_allowed.set()

    def __on_connect_error(self, data) -> None:
        """
        This gets events whenever there's an error during connection attempts.
        The specific case that is handled below is triggered when the connection
        is made with the auth parameter not matching the session ID on the server.
        If this error occurs it's because the connection is attempted towards an
        instance of locust not started by this CLI.

        In that case:
        Cancel the running threading.Timer that would abort reconnect attempts
        and raise a WebsocketTimeout exception.
        Set an exception on the websocket that will be raised from the wait method.
        Cancel further reconnect attempts.
        Set the threading.Event __shutdown_allowed on the websocket that tells the
        wait method that it should stop blocking.
        """
        # Do nothing if it's not the specific case we know how to deal with
        if not (isinstance(data, dict) and data.get("message") == "Session mismatch"):
            return

        self.__connect_timeout_timer.cancel()
        self.exception = SessionMismatchError(
            "The session from this run of locust-cloud did not match the one on the server"
        )
        self.sio._reconnect_abort.set()
        self.__shutdown_allowed.set()
