# Lab Results - Pathology and Screening Adaptor

## Adaptor Scope

The main objective of the Lab Results Adaptor is to hide complex legacy standards and instead present a simple and consistent interface aligned to current NHSD national standards. The adaptor removes the requirement for a GP System to handle the complexities of EDIFACT and MESH messaging.

At a high level, the Lab Results Adaptor exposes a queue ([AMQP 1.0](https://www.amqp.org/resources/specifications)) from which the GP System can consume Pathology and Screening medical reports in [FHIR STU3](https://digital.nhs.uk/developer/guides-and-documentation/api-technologies-at-nhs-digital#fhir) format.

See the [Resources](#resources) section for links to the underlying services and standards. 

Adaptor handles the following types of EDIFACT messages:
- Pathology `NHS002` and `NHS003`
- Screening `NHS004`

and replies with an `NHS001` (NHSACK) message if requested.

## Architecture

![Adaptor Component Diagram](/documentation/lab_results_diagram.png)
![Adaptor Sequence Diagram](/documentation/lab_results_sequence_diagram.png)

Adaptor downloads EDIFACT messages from MESH mailbox (`PATH_MEDRPT_V3` and `SCRN_BCS_MEDRPT_V4` MESH workflows) using MESH Client component and puts them on an internal `Inbound MESH Queue`. The Message Translator component fetches messages off the queue, translates the EDIFACT into FHIR format and puts the result onto the `Outbound GP Queue` where other GP Supplier apps can consume medical reports from.
In the end, if the EDIFACT Interchange Header has requested such, an NHSACK confirmation will be sent back to the laboratory MESH mailbox.

The [Integration Adaptors Github repository](https://github.com/nhsconnect/integration-adaptors) contains an example deployment configuration into AWS infrastructure. For EDIFACT messages with size less than 1 MB, performance tests were executed on AWS Fargate with task memory (MiB)4096 and task CPU (unit)1024.
It is recommended to have more than 1 instance of the adaptor running in a cluster for failover purposes.

### Performance

Adaptor's EDIFACT to FHIR translation steps are built for high performance and resiliency by applying horizontal scaling techniques. However, the MESH consumer's step performance cannot be increased this way. Due to the nature of MESH, if there are multiple adaptor instances, only one randomly chosen instance will download messages and put them on the `Inbound MESH Queue`.

### Message processing and error handling

EDIFACT messages are downloaded from MESH using MESH Client component. Due to specifics on how MESH works, the following design has been implemented.
![MESH Client](/documentation/lab_results_mesh_client_diagram.png)
The MESH Client component is triggered at a given interval (that is specified in the app configuration) where it downloads all messages matching Pathology and Screening workflows and sends back an infrastructure ACK telling MESH to delete downloaded EDIFACT messages. Even if there are multiple instances of the adaptor running, only one of them will be downloading messages at a given time.

When an EDIFACT message is downloaded from MESH using the MESH Client component, its structure is not verified at this step. The message is stored on the internal `Inbound MESH Queue` and MESH is instructed that the file can be deleted since the message has been successfully downloaded.

Two error scenarios are possible at this stage:
- Message has been downloaded but the adaptor was unable to put it on the `Inbound MESH Queue`

    Message has not been put on the queue, nor MESH has been instructed to delete it. Apart from error log entry, there is no side effect. The operation is retried on the next MESH scan cycle.
- Message has been downloaded and placed on `Inbound MESH Queue` but an error occurred while notifying MESH that the message can be deleted

    Message has been put on the queue, but MESH has not been instructed to delete it.
    There are no side effects apart from error log entry. The message has not been deleted from MESH, so it will be fetched again on the next MESH scan cycle. Since the message has already been put on `Inbound MESH Queue`, it will be processed again causing duplicates to appear on the `Outbound GP Queue`. Handling duplicates is described in the  [duplicates](#duplicates) section.

Once the message is on the `Inbound MESH Queue` the Message Translator component picks each one up and performs the following steps:

1) Initial verification

    This step parses the EDIFACT message and verifies the general structure of the message and can result in two types of errors:
    - Interchange Header (UNB) is missing mandatory fields (sender, recipient, sequence number, NHSACK request flag).

        Since the header can't be parsed, an NHSACK can't be produced to inform the laboratory about the error. This is a critical error and manual intervention is required. Message is rejected causing it to be placed back on the `Inbound MESH Queue`. It is picked up again for retry until the MQ max retry value is reached. After that, message is placed on the Dead Letter Queue and manual intervention is required.
    - Interchange Trailer (UNZ) message count or Message Header (UNH) or Message Trailer (UNT) can't be parsed.

        Since the Interchange Header was parsed, all the information required to produce an NHSACK is available. If Interchange Header has the NHSACK request flag set, an NHSACK is produced and sent back to the laboratory MESH mailbox.

2) Translation

    At this step an EDIFACT has already been parsed and split into messages. Each message is translated into a FHIR Bundle. Any error occurring at this step is logged, and if an NHSACK is requested, appropriate information is set in it. Because of the error, no FHIR Bundle is created for that single message. Other messages are not affected and can still be processed. At the end of this step, a summary NHSACK is generated containing information about all the successes and failures of EDIFACT messages processing.

3) Sending to `Outbound GP Queue`

    All individual EDIFACT messages that have been successfully translated into FHIR are sent to `Outbound GP Queue`. If an error occurs at this step, the whole EDIFACT is rejected and it returns to the `Inbound MESH Queue` to be picked up again until MQ max retry value is reached. After that, the EDIFACT is placed on the Dead Letter Queue and manual intervention is required.

4) Sending NHSACK to `Outbound MESH Queue`

    All individual FHIR messages have been sent to the `Outbound GP Queue`. If an NHSACK was requested, it has been generated and sent to `Outbound MESH Queue`. If an error occurs at this step, the whole EDIFACT is rejected and it will be returned to the `Inbound MESH Queue` to be picked up again until MQ max retry value is reached. After that, the EDIFACT is placed on the Dead Letter Queue and manual intervention is required. Since the message has been already put on `Inbound MESH Queue`, it will be processed again causing duplicates to appear on the `Outbound GP Queue`. Handling duplicates is described in the [duplicates](#duplicates) section.

### Logging and debugging

Adaptor provides detailed logs while processing each EDIFACT. Each EDIFACT that goes through the adapter is given a new randomly generated `CorrelationId`. This value is available in every log line that is created while processing that EDIFACT and also as an AMQP header (check [here](#outbound-gp-queue-specification)) on the `Outbound GP Queue`.

### Duplicates

In rare cases, the adaptor can produce duplicate messages on the `Outbound GP Queue`. It is GP Supplier's responsibility to identify and handle duplicates when fetching messages from the queue. The AMQP message `Checksum` header should be used to identify duplicates (see [Outbound GP Queue specification](#outbound-gp-queue-specification)).

## Integrating with the adaptor

When using the adaptor, the GP Supplier will receive medical reports on the `Outbound GP Queue` ([AMQP 1.0](https://www.amqp.org/resources/specifications)) in the form of a [FHIR STU3](https://digital.nhs.uk/developer/guides-and-documentation/api-technologies-at-nhs-digital#fhir) Bundle resources. Examples can be found [here](#examples)

### Outbound GP Queue specification

The `Outbound GP Queue` from where the FHIR-formatted medical reports can be fetched from is an AMQP 1.0 queue. The payload of the queue message is the FHIR Bundle resource accompanied with custom headers:

- `CorrelationId` - a unique identifier assigned for a given inbound EDIFACT message that can be used to trace down message processing and issues in adaptor logs. Keep in mind that a single EDIFACT can contain multiple messages, which will produce multiple queue messages having the same `CorrelationId`. Example: `83035310E0B54283A5242C8EC9CA5FD6`
- `Checksum` - an MD5 checksum of the inbound EDIFACT message. This value should be used to handle duplicates on the GP Supplier Applications end.
- `MessageType` - one of [`Pathology`, `Screening`] indicating the source EDIFACT message type

### Examples

Examples of both Pathology and Screening messages are provided as part of the adaptor's User Acceptance Tests.
Each example is built from 2 files:

- `<example-id>.edifact.dat` - EDIFACT message provided by the laboratory
- `<example-id>.fhir.json` - message translated to FHIR format

Example input EDIFACT files can be found [here](https://github.com/nhsconnect/integration-adaptor-lab-results/tree/main/src/intTest/resources/edifact) with the corresponding FHIR translations found [here](https://github.com/nhsconnect/integration-adaptor-lab-results/tree/main/src/intTest/resources/fhir)

Pathology examples can be found [here](https://github.com/nhsconnect/integration-adaptor-lab-results/tree/main/src/intTest/resources/success_uat_data/NHS003)
Screening examples can be found [here](https://github.com/nhsconnect/integration-adaptor-lab-results/tree/main/src/intTest/resources/success_uat_data/NHS004)

## Resources

[Lab Results adaptor](https://digital.nhs.uk/developer/api-catalogue/lab-results-adaptor)

[Pathology Messaging - EDIFACT API](https://digital.nhs.uk/developer/api-catalogue/pathology-messaging-edifact)

[Bowel Cancer Screening - EDIFACT API](https://digital.nhs.uk/developer/api-catalogue/bowel-cancer-screening-edifact)

[Pathology & Diagnostics Information Standards Collaboration Space](https://hscic.kahootz.com/connect.ti/PathologyandDiagnostics/groupHome)

[National Pathology FHIR Messaging Specifications](https://developer.nhs.uk/apis/itk3nationalpathology-1-1-0/index.html)

[Messaging Specification](https://hscic.kahootz.com/connect.ti/PathologyandDiagnostics/view?objectId=13046960#13046960)

[EDIFACT Specification](https://webarchive.nationalarchives.gov.uk/20150107145848/http:/www.isb.nhs.uk/documents/isb-1557/amd-39-2003)

[FHIR UK Core](https://digital.nhs.uk/services/fhir-uk-core)

## FHIR specification discrepancies

Due to the nature of EDIFACT messages, following discrepancies between FHIR profiles and actual resources have been introduced:

- `DiagnosticReport.identifier.value` - EDIFACT identifier will be used which might not be an UUIDv4.
- `DiagnosticReport.identifier.system` - field will be omitted.
- `Practitioner.identifier.value` - EDIFACT does not provide SDS User ID. Value from EDIFACT will be used which might not be an UUIDv4.
- `Practitioner.identifier.system` - field will be omitted.
- `ProcedureRequest.reasonReference` - field becomes optional.
- `Specimen.accessionIdentifier` - field becomes optional.
- `Specimen.collection` - field becomes optional.
- `Specimen.identifier` - EDIFACT has nothing to map from. FHIR field will be omitted.
- `Specimen.type.coding` - EDIFACT does not give Read2 nor SNOMED. `coding.system` will be omitted. Only `coding.display` will be populated.
- `Observation.code.coding.code` - field becomes optional.
- `Observation.code.coding.display` - field becomes optional.
- `Observation.identifier` - EDIFACT has nothing to map from. FHIR field will be omitted.
- `Observation.value[x]` - can be not only `valueQuantity` but also `valueCodeableConcept`.

## Configuration

All configuration options can be found in the [application.yml](/src/main/resources/application.yml) file. The adaptor reads its configuration from environment variables to override the defaults. The following sections describe the environment variables used to configure the adaptor. 

Variables without a default value and not marked optional *MUST* be defined for the adaptor to run.

### General Configuration

| Environment Variable               | Default                   | Description 
| -----------------------------------|---------------------------|-------------
| LAB_RESULTS_OUTBOUND_SERVER_PORT   | 8080                      | The port on which the adaptor management endpoints will run
| LAB_RESULTS_LOGGING_LEVEL          | INFO                      | Application logging level. One of: DEBUG, INFO, WARN, ERROR. The level DEBUG **MUST NOT** be used when handling live patient data.
| LAB_RESULTS_LOGGING_FORMAT         | (*)                       | Defines how to format log events on stdout

(*) The adaptor uses logback (http://logback.qos.ch/). The built-in [logback.xml](src/main/resources/logback.xml) 
defines the default log format. This value can be overridden using the `LAB_RESULTS_LOGGING_FORMAT` environment variable.
You can provide an external `logback.xml` file using the `-Dlogback.configurationFile` JVM parameter.

### Message Queue Configuration

| Environment Variable                 | Default                   | Description 
| -------------------------------------|---------------------------|-------------
| LAB_RESULTS_MESH_OUTBOUND_QUEUE_NAME | lab_results_mesh_outbound | The name of the internal outbound (to MESH) message queue
| LAB_RESULTS_MESH_INBOUND_QUEUE_NAME  | lab_results_mesh_inbound  | The name of the internal inbound (from MESH) message queue
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
| LAB_RESULTS_MONGO_DATABASE_NAME  | labresults                | Database name for MongoDB
| LAB_RESULTS_MONGO_URI            | mongodb://localhost:27017 | MongoDB connection string

**Trust Store Configuration**

These optional properties configure a trust store with private CA certificates. This trust store does not replace Java's
default trust store. At runtime the application adds these additional certificates to the default trust store. See 
[OPERATING.md - AWS DocumentDB TLS configuration](OPERATING.md#AWS DocumentDB TLS configuration) for more information.

| Environment Variable                      | Default       | Description 
| ------------------------------------------|---------------|-------------
| LAB_RESULTS_SSL_TRUST_STORE_URL           |               | (Optional) URL of the trust store JKS. The only scheme currently supported is `s3://`
| LAB_RESULTS_SSL_TRUST_STORE_PASSWORD      |               | (Optional) Password used to access the trust store

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
| LAB_RESULTS_MESH_RECIPIENT_MAILBOX_ID_MAPPINGS |         | (*) The mapping between each NHSACK recipient to its corresponding MESH Mailbox ID mapping. There is one mapping per line and an equals sign (=) separates the code and mailbox id. For example: "000000004400001=A6840385\000000024600002=A0047392"
| LAB_RESULTS_SCHEDULER_ENABLED                  | true    | Enables/disables automatic MESH message downloads

(*) 15 digit NHS Sender ID required for successful NHSACK dispatch

The following three variables control how often the adaptor performs a MESH polling cycle. During a polling cycle the 
adaptor will download and acknowledge up to "the first 500 messages" (a MESH API limit).

Important: If the MESH mailbox uses workflows other than `PATH_MEDRPT_V3` 
and `SCRN_BCS_MEDRPT_V4`, then these messages must be downloaded and acknowledged by some other means in a timely manner. 
The adaptor will skip messages with other workflow ids leaving them in the inbox. If more than 500 "other" messages 
accumulate the adaptor wil no longer receive new inbound GP Links messages.

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

The following sections provide the necessary information to develop the Lab Results adaptor.

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

    docker-compose build lab-results
    docker-compose up lab-results

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

### Manual Testing

Run `lab-results`, `mongodb`, `activemq` and `fake-mesh` containers:

    docker-compose build
    docker-compose up

Run test script:

    cd ./release/tests
    ./send_message.sh

that will send an [example EDIFACT message](https://github.com/nhsconnect/integration-adaptor-lab-results/blob/main/src/intTest/resources/edifact/pathology.edifact.dat) to the `fake-mesh` container. The `lab-results` container app will fetch this message and put the translated to FHIR format result on the `activemq` container's `lab_results_gp_outbound` queue.
Check [ActiveMQ admin console](http://localhost:8161/admin/queues.jsp) (user: admin, password: admin) to download the result which will match the [example FHIR message](https://github.com/nhsconnect/integration-adaptor-lab-results/blob/main/src/intTest/resources/fhir/pathology.fhir.json). Additionally `mesh/mesh.sh` can be used to fetch the generated NHSACK from `fake-mesh`.

Notice that depending on the `lab-result` configuration, it can take several seconds until `fake-mesh` is scanned for new messages.

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
The `release/tests/send_message.sh` is a test script that sends an [example](https://github.com/nhsconnect/integration-adaptor-lab-results/blob/main/src/intTest/resources/edifact/pathology.edifact.dat) message to MESH mailbox.

#### Fake MESH

A mock implementation of the MESH API is available for local development. The latest version is in Github at
[mattd-kainos/fake-mesh](https://github.com/jamespic/fake-mesh). _It is a fork of [jamespic/fake-mesh](https://github.com/jamespic/fake-mesh)._

The [nhsdev Docker Hub](https://hub.docker.com/repository/docker/nhsdev/fake-mesh) hosts released fake-mesh images.

### Coding Standards

Ensure that you follow the agreed [Java Coding standards](https://gpitbjss.atlassian.net/wiki/spaces/NIA/pages/2108522539/Java+Coding+Standards) on the project when developing and code reviewing the adaptor.
Feel free to update the documentation if you feel anything is incorrect or missing.
