`Locust Cloud <https://locust.cloud/>`_ is a hosted version of Locust that allows you to run distributed load tests without having to set up and maintain your own infrastructure.

It also allows more detailed reporting and analysis, as well as storing historical test results and tracking them over time.

First run
=========

Once you have `signed up <https://locust.cloud/pricing>`_ for Locust Cloud and :ref:`installed Locust <installation>`, you need to authenticate:

.. code-block:: console

    $ locust --cloud --login
    ...

.. note::
    After logging in, an API token will be stored on your machine, and you will not need to log in until it expires.

Then you can launch a distributed test using Locust Cloud:

.. code-block:: console

    $ locust --cloud -f my_locustfile.py --users 100 # ... other regular locust parameters
    [2025-05-13 12:30:58,252] INFO: Deploying (us-east-1, 1.21.5)
    [2025-05-13 12:31:06,366] INFO: Waiting for load generators to be ready...
    [2025-05-13 10:31:11,119] master-lsdhr-8hwhs/INFO/locust.main: Starting Locust 2.37.1 (locust_exporter 1.18.4)
    [2025-05-13 10:31:11,120] master-lsdhr-8hwhs/INFO/locust.main: Starting web interface at https://us-east-1.webui.locust.cloud/your_company, press enter to open your default browser.
    [2025-05-13 10:31:11,760] master-lsdhr-8hwhs/INFO/locust.runners: worker-jdnf6-jl8qq_a45c04a8d925448ea647fdcda2e8cf80 (index 0) reported as ready. 1 workers connected.
    [2025-05-13 10:31:11,765] master-lsdhr-8hwhs/INFO/locust.runners: worker-jdnf6-pk8cl_6749cd6c0d244b3a9611d6a4e0a8d30b (index 1) reported as ready. 2 workers connected.
    ...
