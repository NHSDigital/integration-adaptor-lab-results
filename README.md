# integration-adaptor-lab-results
Integration Adaptor to simplify processing of Pathology and Screening results.

## Configuration

The adaptor reads its configuration from environment variables. The following sections describe the environment variables
 used to configure the adaptor. 
 
Variables without a default value and not marked optional are *MUST* be defined for the adaptor to run.

### General Configuration

| Environment Variable               | Default                   | Description 
| -----------------------------------|---------------------------|-------------
| LAB_RESULTS_OUTBOUND_SERVER_PORT   | 80                        | The port on which the adaptor and management endpoints will run
| LAB_RESULTS_LOGGING_LEVEL          | INFO                      | Application logging level. One of: DEBUG, INFO, WARN, ERROR. The level DEBUG **MUST NOT** be used when handling live patient data.
| LAB_RESULTS_LOGGING_FORMAT         | (*)                       | Defines how to format log events on stdout

(*) The adaptor uses logback (http://logback.qos.ch/). The built-in [logback.xml](src/main/resources/logback.xml) 
defines the default log format. This value can be overridden using the `LAB_RESULTS_LOGGING_FORMAT` environment variable.
You can provide an external `logback.xml` file using the `-Dlogback.configurationFile` JVM parameter.

### Message Queue Configuration

| Environment Variable                 | Default                   | Description 
| -------------------------------------|---------------------------|-------------
| LAB_RESULTS_MESH_OUTBOUND_QUEUE_NAME | lab_results_mesh_outbound | The name of the outbound (to MESH) message queue
| LAB_RESULTS_MESH_INBOUND_QUEUE_NAME  | lab_results_mesh_inbound  | The name of the inbound (from MESH) message queue
| LAB_RESULTS_GP_OUTBOUND_QUEUE_NAME   | lab_results_gp_outbound   | The name of the outbound (to GP System) message queue
| LAB_RESULTS_AMQP_BROKERS             | amqp://localhost:5672     | A comma-separated list of URLs to AMQP brokers (*)
| LAB_RESULTS_AMQP_USERNAME            |                           | (Optional) username for the AMQP server
| LAB_RESULTS_AMQP_PASSWORD            |                           | (Optional) password for the AMQP server
| LAB_RESULTS_AMQP_MAX_REDELIVERIES    | 3                         | The number of times a message will be retried to be delivered to consumer. After exhausting all retries, it will be put on DLQ.<queue_name> dead-letter queue. The default JMS configuration is (-1) disabled.

(*) Active/Standby: The first broker in the list is always used unless there is an error, in which case the other URLs will be used. At least one URL is required.

### MongoDB Configuration Options

The adaptor configuration for MongoDB can be configured two ways, using a connection string or providing individual 
properties. This is to accommodate differences in the capabilities of deployment automation frameworks and varying 
environments.

Option 1: If `LAB_RESULTS_MONGO_HOST` is defined then the adaptor forms a connection string from the following properties:

| Environment Variable             | Default     | Description 
| ---------------------------------|-------------|-------------
| LAB_RESULTS_MONGO_DATABASE_NAME  | labresults  | Database name for MongoDB
| LAB_RESULTS_MONGO_HOST           |             | MongoDB host
| LAB_RESULTS_MONGO_PORT           |             | MongoDB port
| LAB_RESULTS_MONGO_USERNAME       |             | (Optional) MongoDB username. If set then password must also be set.
| LAB_RESULTS_MONGO_PASSWORD       |             | (Optional) MongoDB password
| LAB_RESULTS_MONGO_OPTIONS        |             | (Optional) MongoDB URL encoded parameters for the connection string without a leading ?
| LAB_RESULTS_MONGO_TTL            | P30D        | (Optional) Time-to-live value

Option 2: If `LAB_RESULTS_MONGO_HOST` is undefined then the adaptor uses the connection string provided:

| Environment Variable             | Default                   | Description 
| ---------------------------------|---------------------------|-------------
| LAB_RESULTS_MONGO_DATABASE_NAME  | labresults               | Database name for MongoDB
| LAB_RESULTS_MONGO_URI            | mongodb://localhost:27017 | MongoDB connection string

## MESH API

### MESH API Connection Configuration

Configure the MESH API connection using the following environment variables:

| Environment Variable                           | Default | Description 
| -----------------------------------------------|---------|-------------
| LAB_RESULTS_MESH_MAILBOX_ID                    |         | The mailbox id used by the adaptor to send and receive messages. This is the sender of outbound messages and the mailbox where inbound messages are received.
| LAB_RESULTS_MESH_MAILBOX_PASSWORD              |         | The password for LAB_RESULTS_MESH_MAILBOX_ID
| LAB_RESULTS_MESH_SHARED_KEY                    |         | A shared key used to generate auth token and provided by MESH operator (OpenTest, PTL, etc)
| LAB_RESULTS_MESH_HOST                          |         | The **Complete URL** with trailing slash of the MESH service. For example: https://msg.int.spine2.ncrs.nhs.uk/messageexchange/
| LAB_RESULTS_MESH_CERT_VALIDATION               | true    | "false" to disable certificate validation of SSL connections
| LAB_RESULTS_MESH_ENDPOINT_CERT                 |         | The content of the PEM-formatted client endpoint certificate
| LAB_RESULTS_MESH_ENDPOINT_PRIVATE_KEY          |         | The content of the PEM-formatted client private key
| LAB_RESULTS_MESH_SUB_CA                        |         | The content of the PEM-formatted certificate of the issuing Sub CA. Empty if LAB_RESULTS_MESH_CERT_VALIDATION is false
| LAB_RESULTS_MESH_RECIPIENT_MAILBOX_ID_MAPPINGS |         | (*) The mapping between each recipient HA Trading Partner Code (HA Link Code) to its corresponding MESH Mailbox ID mapping. There is one mapping per line and an equals sign (=) separates the code and mailbox id. For example: "COD1=A6840385\nHA01=A0047392"
| LAB_RESULTS_SCHEDULER_ENABLED                  | true    | Enables/disables automatic MESH message downloads

(*) The three-character "Destination HA Cipher" required for each outbound API request uniquely identifies that patient's 
managing organisation. Each managing organisation also has a four-character "HA Trading Partner Code" (HA Link Code) uniquely
identifying that patient's managing organisation for the purpose of EDIFACT messaging. Finally, each "HA Trading Partner Code"
is assigned a MESH Mailbox ID: the mailbox to which the EDIFACT files for a given recipient are sent. The mappings between
organisations' "HA Trading Partner Codes" and their MESH Mailbox IDs are controlled by this variable. Note: A "Destination HA Cipher" 
can usually be converted into a "HA Link Code" by appending 1 or 01 to create the four-character code. If in doubt consult 
with the operator of the LAB_RESULTS instance for the correct value.

The following three variables control how often the adaptor performs a MESH polling cycle. During a polling cycle the 
adaptor will download and acknowledge up to "the first 500 messages" (a MESH API limit).

Important: If the MESH mailbox uses workflows other than `LAB_RESULTS_REG` and `LAB_RESULTS_RECEP` then these messages must be
downloaded and acknowledged by some other means in a timely manner. The adaptor will skip messages with other workflow
ids leaving them in the inbox. If more than 500 "other" messages accumulate the adaptor wil no longer receive new 
inbound GP Links messages.

| Environment Variable                                       | Default | Description 
| -----------------------------------------------------------|---------|-------------
| LAB_RESULTS_MESH_CLIENT_WAKEUP_INTERVAL_IN_MILLISECONDS    | 60000   | The time period (in milliseconds) between when each adaptor instance "wakes up" and attempts to obtain the lock to start a polling cycle
| LAB_RESULTS_MESH_POLLING_CYCLE_MINIMUM_INTERVAL_IN_SECONDS | 300     | The minimum time period (in seconds) between MESH polling cycles
| LAB_RESULTS_MESH_POLLING_CYCLE_DURATION_IN_SECONDS         | 285     | The duration (in seconds) fo the MESH polling cycle

The MESH API specifies that a MESH mailbox should be checked "a maximum of once every five minutes". The variable 
`LAB_RESULTS_MESH_POLLING_CYCLE_MINIMUM_INTERVAL_IN_SECONDS` controls how often the adaptor will check its mailbox for new 
messages. This should not be set to less than 300 seconds. A time lock in the database prevents the polling cycle from
running more often than this minimum interval. Each adaptor instance will wake up every 
`LAB_RESULTS_MESH_CLIENT_WAKEUP_INTERVAL_IN_MILLISECONDS` to try this time lock. Therefore, the maximum polling cycle interval
is the sum of these two values.

Only one instance of the adaptor runs the polling cycle at any given time to prevent duplicate processing. The value
`LAB_RESULTS_MESH_POLLING_CYCLE_DURATION_IN_SECONDS` prevents one polling cycle from overrunning into the next time interval.
This value must always be less than `LAB_RESULTS_MESH_POLLING_CYCLE_MINIMUM_INTERVAL_IN_SECONDS`.

## Operating

Refer to [OPERATING.md](OPERATING.md) for tips about how to operate the adaptor in the production environment.

## Development

The following sections provide the necessary information to develop the Integration Adaptor.

The adaptor configuration has sensible defaults for local development. Some overrides might be required where the 
"secure by default" principle takes precedence:

* `LAB_RESULTS_MESH_CERT_VALIDATION: "false"` - if using fake-mesh then certificate validation must be disabled
* `LAB_RESULTS_LOGGING_LEVEL: "DEBUG"` - consider using DEBUG logging while developing

### Pre-requisites (IntelliJ)

* Install a Java JDK 11. [AdoptOpenJdk](https://adoptopenjdk.net/index.html?variant=openjdk11&jvmVariant=hotspot) is recommended.
* Install [IntelliJ](https://www.jetbrains.com/idea/)
* Install the [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok). Intellij should prompt you to enable annotation processing, ensure you enable this.
* Install [Docker](https://www.docker.com/products/docker-desktop)

### Import the integration-adaptor-lab-results project

* Clone this repository
* Open the cloned `integration-adaptor-lab-results` folder
* Click pop-up that appears: (import gradle daemon)
* Verify the project structure


    Project structure -> SDKs -> add new SDK -> select adoptopenjdk-11.jdk/Contents/Home  (or alternative location)
                      -> Project SDK -> Java 11 (11.0.9)
                      -> Module SDK -> Java 11 (11.0.9)

### Start Dependencies

* [mongo](https://hub.docker.com/_/mongo/): MongoDB Docker images
* [rmohr/activemq](https://hub.docker.com/r/rmohr/activemq): ActiveMQ Docker images
* [nhsdev/fake-mesh](https://hub.docker.com/r/nhsdev/fake-mesh): fake-mesh (mock MESH API server) Docker images

Run `docker-compose up mongodb activemq fake-mesh`

### Running

**From IntelliJ**

Navigate to: IntegrationAdapterLabResultsApplication -> right click -> Run

**Inside a container**

    export BUILD_TAG=latest
    docker-compose build lab-results
    docker-compose up lab-results

**Inside multiple containers, behind a load balancer**

Docker Compose allows running multiple instances behind a nginx load balancer in using round-robin routing.

    export BUILD_TAG=latest
    docker-compose build lab-results
    docker-compose -f docker-compose.yml -f docker-compose.lb.override.yml up --scale lab-results=3 lab-results

This command will start three instances of the adaptor behind a load balancer on port 8080.

To change the scale number while all services are running run the same "up" command with new scale value and then
restart the load balancer container (so it will become aware of instance count change).

### Running quality checks

**All quality checks**
    
    ./gradlew check -x test -x integrationTest
    
This runs Spotbugs and Checkstyle to perform a static analysis to find potential bugs and checks to see if the code style conforms to the [Java Coding standards](https://gpitbjss.atlassian.net/wiki/spaces/NIA/pages/2108522539/Java+Coding+Standards).

**Checkstyle checks**
    
    ./gradlew checkstyleIntTest checkstyleMain checkstyleTest
    
**Spotbugs checks**

    ./gradlew spotbugsMain
    
SpotbugsMain is the only Spotbugs task we run when executing `./gradlew check`.

### Running Tests

**All Tests**

    ./gradlew check -x spotbugsMain -x spotbugsIntTest -x spotbugsTest -x checkstyleMain -x checkstyleIntTest -x checkstyleTest

**Unit Tests**

This will run all tests inside the [src/test](./src/test) folder.

    ./gradlew test

**Integration Tests**

A separate source folder [src/intTest](./src/intTest) contains integration tests. To run the integration tests use:

    ./gradlew integrationTest
    
**All Tests and Checks**

This command will run all tests (unit & integration) and all static analysis and code style checks. 

The `--continue` flag ensures that all tests and checks will run.

    ./gradlew check --continue

### Debugging

#### MongoDB

To view data in MongoDB:

* Download [Robo 3T](https://robomongo.org/)
* Open Robo 3T -> Create new connection with details as below:
  * Type: Direct Connection
  * Name: labresults
  * Address: localhost : 27017
* View adaptor collections by navigating to labresults -> collections -> (select any collection)

#### ActiveMQ

To view messages in the ActiveMQ Web Console:

* Open browser and navigate to: http://localhost:8161/
  * Username: admin
  * Password: admin
* Click manage ActiveMQ broker
* Click Queues tab
* Select desired queue
* Select a message ID to display information of message 

#### MESH API

A `mesh.sh` bash script exists for testing or debugging MESH. For more information see: [mesh/README.md](/mesh/README.md)

#### Fake MESH

A mock implementation of the MESH API is available for local development. The latest version is in Github at
[mattd-kainos/fake-mesh](https://github.com/jamespic/fake-mesh). _It is a fork of [jamespic/fake-mesh](https://github.com/jamespic/fake-mesh)._

The [nhsdev Docker Hub](https://hub.docker.com/repository/docker/nhsdev/fake-mesh) hosts released fake-mesh images.

### Coding Standards

Ensure that you follow the agreed [Java Coding standards](https://gpitbjss.atlassian.net/wiki/spaces/NIA/pages/2108522539/Java+Coding+Standards) on the project when developing and code reviewing the adaptor.
Feel free to update the documentation if you feel anything is incorrect or missing.
