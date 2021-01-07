# integration-adaptor-lab-results
Integration Adaptor to simplify processing of Pathology and Screening results

## Development

The following sections provide the necessary information to develop the Integration Adaptor.

### Pre-requisites (IntelliJ)

* Install a Java JDK 11. [AdoptOpenJdk](https://adoptopenjdk.net/index.html?variant=openjdk11&jvmVariant=hotspot) is recommended.
* Install [IntelliJ](https://www.jetbrains.com/idea/)
* Install the [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok)

### Import the integration-adaptor-lab-results project

* Clone this repository
* Open the cloned `integration-adaptor-lab-results` folder
* Click pop-up that appears: (import gradle daemon)
* Verify the project structure

### Start Dependencies

* [rmohr/activemq](https://hub.docker.com/r/rmohr/activemq): ActiveMQ Docker images

[comment]: <> (* [nhsdev/fake-mesh]&#40;https://hub.docker.com/r/nhsdev/fake-mesh&#41;: fake-mesh &#40;mock MESH API server&#41; Docker images)

[comment]: <> (Run `docker-compose up activemq fake-mesh`)
Run `docker-compose up activemq`

### Running

**From IntelliJ***

Navigate to: IntegrationAdapterLabResultsApplicationTests -> right click -> Run

**Inside a container**

    export BUILD_TAG=latest
    docker-compose build lab-results
    docker-compose up lab-results

### Running Tests

**All Tests**

    ./gradlew check

**Integration Tests**

A separate source folder [src/intTest](./src/intTest) contains integration tests. To run the integration tests use:

    ./gradlew integrationTest
