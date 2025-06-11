import sys
import time
import webbrowser

import requests
from locust_cloud.common import VALID_REGIONS, CloudConfig, delete_cloud_config, get_api_url, write_cloud_config

POLLING_FREQUENCY = 1


def web_login() -> None:
    print("Enter the number for the region to authenticate against")
    print()
    for i, valid_region in enumerate(VALID_REGIONS, start=1):
        print(f"  {i}. {valid_region}")
    print()
    choice = input("> ")
    try:
        region_index = int(choice) - 1
        assert 0 <= region_index < len(VALID_REGIONS)
    except (ValueError, AssertionError):
        print(f"Not a valid choice: '{choice}'")
        sys.exit(1)

    region = VALID_REGIONS[region_index]

    try:
        response = requests.post(f"{get_api_url(region)}/cli-auth")
        response.raise_for_status()
        response_data = response.json()
        authentication_url = response_data["authentication_url"]
        result_url = response_data["result_url"]
    except Exception as e:
        print("Something went wrong trying to authorize the locust-cloud CLI:", str(e))
        sys.exit(1)

    message = f"""
Attempting to automatically open the SSO authorization page in your default browser.
If the browser does not open or you wish to use a different device to authorize this request, open the following URL:

{authentication_url}
    """.strip()
    print()
    print(message)

    webbrowser.open_new_tab(authentication_url)

    while True:  # Should there be some kind of timeout?
        response = requests.get(result_url)

        if not response.ok:
            print("Oh no!")
            print(response.text)
            sys.exit(1)

        data = response.json()

        if data["state"] == "pending":
            time.sleep(POLLING_FREQUENCY)
            continue
        elif data["state"] == "failed":
            print(f"\nFailed to authorize CLI: {data['reason']}")
            sys.exit(1)
        elif data["state"] == "authorized":
            print("\nAuthorization succeded. Now you can re-run locust --cloud without the --login flag.")
            break
        else:
            print("\nGot unexpected response when authorizing CLI")
            sys.exit(1)

    config = CloudConfig(
        id_token=data["id_token"],
        refresh_token=data["refresh_token"],
        user_sub_id=data["user_sub_id"],
        refresh_token_expires=data["refresh_token_expires"],
        id_token_expires=data["id_token_expires"],
        region=region,
    )
    write_cloud_config(config)


def logout():
    delete_cloud_config()
