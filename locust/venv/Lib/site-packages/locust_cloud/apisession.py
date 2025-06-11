import logging
import os
import sys
import time

import requests
from locust_cloud.common import VALID_REGIONS, __version__, get_api_url, read_cloud_config, write_cloud_config

logger = logging.getLogger(__name__)

unauthorized_message = "You need to log in again. Please run:\n    locust --cloud --login"


class ApiSession(requests.Session):
    def __init__(self, non_interactive: bool) -> None:
        super().__init__()
        self.non_interactive = non_interactive

        if non_interactive:
            username = os.getenv("LOCUSTCLOUD_USERNAME")
            password = os.getenv("LOCUSTCLOUD_PASSWORD")
            region = os.getenv("LOCUSTCLOUD_REGION")

            if not all([username, password, region]):
                print(
                    "Running with --non-interactive requires that LOCUSTCLOUD_USERNAME, LOCUSTCLOUD_PASSWORD and LOCUSTCLOUD_REGION environment variables are set."
                )
                sys.exit(1)

            if region not in VALID_REGIONS:
                print("Environment variable LOCUSTCLOUD_REGION needs to be set to one of", ", ".join(VALID_REGIONS))
                sys.exit(1)

            self.__configure_for_region(region)
            response = requests.post(
                self.__login_url,
                json={"username": username, "password": password},
                headers={"X-Client-Version": __version__},
            )
            if not response.ok:
                print(f"Authentication failed: {response.text}")
                sys.exit(1)

            id_token = response.json()["cognito_client_id_token"]
            user_sub_id = response.json()["user_sub_id"]
            refresh_token = response.json()["refresh_token"]
            id_token_expires = response.json()["id_token_expires"]
        else:
            config = read_cloud_config()

            if config.refresh_token_expires < time.time() + 24 * 60 * 60:
                print(unauthorized_message)
                sys.exit(1)

            assert config.region
            self.__configure_for_region(config.region)
            id_token = config.id_token
            user_sub_id = config.user_sub_id
            refresh_token = config.refresh_token
            id_token_expires = config.id_token_expires

        assert id_token

        self.__user_sub_id = user_sub_id
        self.__refresh_token = refresh_token
        self.__id_token_expires = id_token_expires - 60  # Refresh 1 minute before expiry
        self.headers["Authorization"] = f"Bearer {id_token}"
        self.headers["X-Client-Version"] = __version__

    def __configure_for_region(self, region: str) -> None:
        self.region = region
        self.api_url = get_api_url(region)
        self.__login_url = f"{self.api_url}/auth/login"

        logger.debug(f"Lambda url: {self.api_url}")

    def __ensure_valid_authorization_header(self) -> None:
        if self.__id_token_expires > time.time():
            return
        if not self.__user_sub_id and self.__refresh_token:
            print(unauthorized_message)
            sys.exit(1)

        response = requests.post(
            self.__login_url,
            json={"user_sub_id": self.__user_sub_id, "refresh_token": self.__refresh_token},
            headers={"X-Client-Version": __version__},
        )

        if not response.ok:
            logger.error(f"Authentication failed: {response.text}")
            sys.exit(1)

        # TODO: Technically the /login endpoint can return a challenge for you
        #       to change your password.
        #       Now that we have a web based login flow we should force them to
        #       do a locust --cloud --login if we get that.

        id_token = response.json()["cognito_client_id_token"]
        id_token_expires = response.json()["id_token_expires"]
        self.__id_token_expires = id_token_expires - 60  # Refresh 1 minute before expiry
        self.headers["Authorization"] = f"Bearer {id_token}"

        if not self.non_interactive:
            config = read_cloud_config()
            config.id_token = id_token
            config.id_token_expires = id_token_expires
            write_cloud_config(config)

    def request(self, method, url, *args, **kwargs) -> requests.Response:
        self.__ensure_valid_authorization_header()
        return super().request(method, f"{self.api_url}{url}", *args, **kwargs)
