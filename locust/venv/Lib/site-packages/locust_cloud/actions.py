import logging

logger = logging.getLogger(__name__)


def delete(session):
    try:
        logger.info("Tearing down Locust cloud...")
        response = session.delete(
            "/teardown",
        )

        if response.status_code == 200:
            logger.debug(f"Response message from teardown: {response.json()['message']}")
        else:
            logger.info(
                f"Could not automatically tear down Locust Cloud: HTTP {response.status_code}/{response.reason} - Response: {response.text} - URL: {response.request.url}"
            )
    except KeyboardInterrupt:
        pass  # don't show nasty callstack
    except Exception as e:
        logger.error(f"Could not automatically tear down Locust Cloud: {e.__class__.__name__}:{e}")
