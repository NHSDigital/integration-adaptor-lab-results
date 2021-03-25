# Lab Results Responder

As described in Lab Results [README.md](../README.md), if the adaptor has been configured 
to not generate NHSACK automatically, it is required by the GP consumer application
to provide an ACK message containing information if FHIR passed GP validation.

The purpose of this application is to automatically respond to Outbound GP Queue messages 
(FHIR med reports) coming from the Lab Results adaptor.

**The Lab Results Responder should be used only for testing purpose**

## Configuration

| environment variable                        | default                 | description                                                                                     |
|---------------------------------------------|-------------------------|-------------------------------------------------------------------------------------------------|
| LAB_RESULTS_RESPONDER_SERVER_PORT           | 8081                    | Server port on which the application exposes the /healthcheck endpoint                          |
| LAB_RESULTS_RESPONDER_LOGGING_LEVEL         | INFO                    | One of DEBUG, INFO, WARN, ERROR                                                                 |
| LAB_RESULTS_RESPONDER_MODE                  | SUCCESS                 | Defines which template to use when responding. One of SUCCESS, ERROR, RANDOM                    |
| LAB_RESULTS_RESPONDER_AMQP_BROKERS          | amqp://localhost:5672   | URL to AMQP borkers (should be same as in Lab Results adaptor)                                  |
| LAB_RESULTS_RESPONDER_OUTBOUND_QUEUE_NAME   | lab_results_gp_outbound | Outbound GP Queue to listen on for new FHIR messages (should be same as in Lab Results adaptor) |
| LAB_RESULTS_RESPONDER_INBOUND_QUEUE_NAME    | lab_results_gp_inbound  | Inbound GP Queue to send acknowledgment to (should be same as in Lab Results adaptor)           |
| LAB_RESULTS_RESPONDER_AMQP_USERNAME         |                         | AMQP brokers user name                                                                          |
| LAB_RESULTS_RESPONDER_AMQP_PASSWORD         |                         | AMQP brokers user password                                                                      |
| LAB_RESULTS_RESPONDER_AMQP_MAX_REDELIVERIES | 3                       | Defines max amount of times message processing should be retried before putting it on DLQ       |

## Running the Responder

a) As a standalone Java application

```
./gradlew bootRun
```

b) As a Docker container together with all other Lab Results components using Lab Results [`docker-compose.yml`](../docker-compose.yml)

```
docker-compose build; docker-compose up
```