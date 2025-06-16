# Volumetric and Performance testing

There are 2 ways to put load on the Lab Results adaptor in order to measure its performance.
* sending EDIFACT to MESH mailbox as a laboratory would
* putting EDIFACT messages directly on `Inbound MESH Queue`

The latter is omitting the step where the adaptor is scanning MESH for new messages.

For both ways a sample EDIFACT file should be present on disk.

## Sending EDIFACT to MESH

Use the MESH Client script to send the same file multiple times to a given MESH mailbox. Details about the script can be found [here](../mesh/README.md).

Example:

    for ((i = 1 ; i <= 3; i++)); do ./mesh.sh send lab_results_mailbox "@../src/intTest/resources/edifact/pathology_3.edifact.dat"; done

will send the content of a file located at `../src/intTest/resources/edifact/pathology_3.edifact.dat` to `lab_results_mailbox` MESH mailbox 3 times.

## Sending EDIFACT to `Inbound MESH Queue`

Use the ["A" application](https://github.com/fmtn/a) which is an AMQP client allowing you to send messages directly to `Inbound MESH Queue`. Java 21 required.

Wrap any Med Report EDIFACT message into json (example: [pathology_3.edifact.dat](`../src/intTest/resources/edifact/pathology_3.edifact.dat`)) as follows:
```json
{
    "recipient": null,
    "workflowId": "PATH_MEDRPT_V3",
    "content": "<EDIFACT>",
    "messageSentTimestamp": "2021-03-25T15:31:29.175Z[Europe/London]",
    "meshMessageId": "20210325153127453385_000000111"
}
```
and save as `message.json`. Run the `A` app to send the message.

Example:

    java -jar a-1.5.0-jar-with-dependencies.jar -A -b "amqps://admin:pwd@my-mq.com:5671" -p "@message.json" -c 5 lab_results_mesh_inbound

This will send the content of the `message.json` file 5 times to the `lab_results_mesh_inbound` queue on `my-mq.com` broker listening on port `5671` connecting using secured AMQP protocol (amqp<span style="color:yellow">**s**</span>), user name `admin` and password `pwd`
