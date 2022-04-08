# Quick reference
- Maintained by: NHS Digital
- Where to get help: https://github.com/nhsconnect/integration-adaptor-lab-results
- Where to file issues: https://github.com/nhsconnect/integration-adaptor-lab-results/issues

# What is the Lab Results Adaptor?
* A pre-assured implementation of Pathology and Screening FHIR adapter

# How to use this image

To help you begin using the Lab Results Adaptor we provide shell scripts and Docker Compose configurations.

## Clone the repository

```bash
git clone https://github.com/nhsconnect/integration-adaptor-lab-results.git
```

## Pull the latest changes and checkout the release tag

Every tagged container on Docker Hub has a corresponding tag in the GitHub repository. Checkout the tag of the release 
you are testing to ensure compatibility with configurations and scripts.

```bash
git pull
git checkout 0.0.6
```

## Configure the application

`docker-compose.yml` is preconfigured with environment values to run a standalone version of the adaptor using fake mesh.

If you would like to run the adaptor against any other Mesh service or using any other database or message queue broker, modify required environment variables as described in to the [README](https://github.com/nhsconnect/integration-adaptor-lab-results/blob/main/README.md)

## Find the release directory

```bash
cd ../release
```

## Start the adaptor

The script pulls the released Lab Results adaptor container image from Docker Hub. It builds containers for its dependencies
from the Dockerfiles in the repository.

```bash
./run.sh
```

## Monitor the logs

```bash
./logs.sh
```

## Run the tests

We provide shell scripts in the release/tests directory to help you start testing.

* `healthcheck.sh` verifies that the adaptor's healthcheck endpoint is available
* `send_message.sh` puts an example EDIFACT message on the fake mesh allowing it to be consumed and translated to FHIR by the adaptor

Set all required environment variables as described in MESH Client README [Setup](../mesh/README.md#setup) and [Environment set up](../mesh/README.md#environment-set-up) sections

```bash
cd tests/
./healthcheck.sh
./send_message.sh
```

## Stopping the adaptor
```bash
cd ../docker
docker-compose down
```
