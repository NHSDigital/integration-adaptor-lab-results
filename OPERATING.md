# Operating

This document contains requirements and tips for operating the adaptor in a production environment.

# AMQP Message Broker Requirements

* The broker must be configured with a limited number of retries and dead-letter queues
* It is the responsibility of the GP supplier to configure adequate monitoring against the dead-letter queues that allows ALL undeliverable messages to be investigated fully
* The broker must use persistent queues to avoid loss of data
* The GP System must persist the relevant message data before acknowledging the message from the queue to avoid loss of data

**Using AmazonMQ**

* A persistent broker (not in-memory) must be used to avoid data loss
* A configuration profile that includes settings for [retry and dead-lettering](https://activemq.apache.org/message-redelivery-and-dlq-handling.html) must be applied
* AmazonMQ uses the scheme `amqp+ssl://` but this **MUST** be changed to `amqps://` when configuring the adaptor

**Using Azure Service Bus**

* The ASB must use [MaxDeliveryCount and dead-lettering](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-dead-letter-queues#exceeding-maxdeliverycount)
* Azure Service Bus may require some parameters as part of the URL configuration. For example: `LAB_RESULTS_AMQP_BROKERS=amqps://<NAME>.servicebus.windows.net/;SharedAccessKeyName=<KEY NAME>;SharedAccessKey=<KEY VALUE>`

# MongoDB Database Requirements

* The Lab Results Adaptor and Lab Results system communications synchronise through a sequence number mechanism
* The MongoDB database preserves this synchronisation
* Deleting the MongoDB database and/or its collections will break the link with the Lab Results system

**Amazon Document DB Tips**

In the "Connectivity & security" tab of the cluster a URI is provided to "Connect to this cluster with an application".
Replace \<username\>:\<insertYourPasswordHere\> with the actual MongoDB username and password to be used by the application.
The value of `LAB_RESULTS_MONGO_URI` should be set to this value. Since the URI string contains credentials we recommend 
managing the entire value as a secured secret.

The user must have the `readWrite` role or a custom role with specific privileges.

## Database Collections

The default database name is `labresults` but this can be changed through an environment variable. Each deployment of the
adaptor MUST have its own database, but multiple databases could be hosted by a single cluster. The collection names
used by the adaptor cannot be changed.

### Outbound Sequence Ids

Tracks the sequence numbers used to "link" a GP and to HA using EDIFACT messaging. See 
"Linking a GP Practice to a Lab Results system" section below for more information.

Collection Name: `outboundSequenceId`

Properties:

* `_id` the key for the sequence in the format \<type\>-\<sender\>-\<recipient\> where:
  * \<type\> is one of SIS (send interchange sequence), SMS (send message sequence)
  * \<sender\> is the GP Trading Partner Code
  * \<recipient\> is the HA Trading Partner Code
* `sequenceNumber` is the most recently generated number for the sequence

Example:

    {
        "_id" : "SIS-TES5-XX11",
        "sequenceNumber" : 2
    }


### Scheduler Timestamp

A collection used to coordinate running the MESH polling cycle across multiple instances of the adaptor. README 
describes this mechanism in more detail.

Collection Name: `schedulerTimestamp`

Properties:

* `_id`: ObjectId generated by the database system. Not used by the adaptor.
* `schedulerType`: Always "meshTimestamp"
* `updateTimestamp`: The time the MESH polling cycle last ran
* `_class`: Automatically generated by the Spring framework

Example:

    {
        "_id" : ObjectId("5f638befb3bd281ba51000a7"),
        "schedulerType" : "meshTimestamp",
        "updateTimestamp" : ISODate("2020-10-08T11:55:43.520Z"),
        "_class" : "uk.nhs.digital.nhsconnect.lab.results.mesh.scheduler.SchedulerTimestamp"
    }

# MESH Requirements

**Note**: The "Development" section of the README refers to a fake-mesh component. fake-mesh is **not** part of the 
adaptor  solution and should only be used to assist local development.

NHSD manage access to MESH, allocate mailboxes, and provide connection details / credentials.

# Management Endpoints

[Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready) provides
three management endpoints:

* /healthcheck
* /metrics
* /info

# Logging and Tracing

The adaptor follows usual conventions for containerised solutions and logs to the console. The chosen container 
orchestration solution must be configured to store and aggregate the adaptor's logs.

## Correlation Id

The adaptor uses a correlation id to make tracing individual requests easier. All log lines will contain the text 
`CorrelationId=<Correlation Id>`. For outbound requests the value of `<Correlation Id>` is either declared in the 
request headers or randomly generated.

For inbound messages the adaptor generates a correlation id for every message (interchange) downloaded from MESH. 
An interchange may contain multiple messages, and the adaptor uses the same correlation id for all messages 
within an interchange.

Some log lines are uncorrelated to a specific message. These logs have an empty `<Correlation Id>` value.

Messages published to the adaptor's three AMQP message queues include a CorrelationId header.

# Linking a GP Practice to a Lab Results system

The Lab Results Adaptor and Lab Results system communications synchronise through a sequence number mechanism. Linking a GP 
Practice to a Lab Results system which have never previously exchanged messages requires no additional setup for 
synchronisation. All the sequences begin at 1, and the adaptor will start them automatically.

In the case that a new market entrant GP System takes over from an incumbent system the new system must pick up the 
sequences where the incumbent left off. For every GP/Lab Results link established, the incumbent supplier or Lab Results operator 
must advise the following:

* Most recently used Send Interchange Sequence (SIS) number, GP -> HA
* Most recently used Send Message Sequence (SMS) number, GP -> HA

For each GP/Lab Results pair the following documents must be inserted into the `outboundSequenceId` collection of the 
adaptor's database. The angle-bracketed values must be replaced (including the brackets) with the relevant data items.
The `_id` property should have the type `String`, and the `sequenceNumber` property should have the type `int32`. Any 
existing documents with the same `_id` must be replaced.

    {
        _id: 'SIS-<GP Link (Trading Partner) Code>-<HA Link (Trading Partner) Code>',
        sequenceNumber: <Send Interchange Sequence (SIS) number>
    }
    
    {
        _id: 'SMS-<GP Link (Trading Partner) Code>-<HA Link (Trading Partner) Code>',
        sequenceNumber: <Send Message Sequence (SMS) number>
    }