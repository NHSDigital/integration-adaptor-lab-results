# MESH Command Line Tool

A command line tool to use with [NHS Digital's MESH API](https://meshapi.docs.apiary.io/) for testing purposes.

## Setup

Copy `env.example.sh` to `env.sh` and fill in the values as described in the comments and below.

You **MUST** provide files containing **your** MESH private key and endpoint certificate.
Copy the values into either the path/files used by default
(see `env.example.sh` for instruction) or to paths/files of your choosing (provided you set the variables
accordingly)

## Usage

For all commands `[mailbox_id]` is the id of the mailbox used for the request. The `MAILBOX_PASSWORD` environment
variable must be correct for the given id.

### Environment set up

Invoke the desired `env.sh` file to load environment variables that will be used by the mesh client

Your own values:

    source env.sh

Predefined fake mesh values:

    source env.fake-mesh.sh

### Authenticate

MESH API docs says to do this first, but in our experience it seems that it's not needed.

    ./mesh.sh auth
    
### List Inbox Messages

Performs the "Check inbox" operation. The JSON response is printed to the console.

    ./mesh.sh inbox [mailbox_id]
    
### Send a message

Send a message with message content provided on the command line

    ./mesh.sh send [mailbox_id] "message_content" [count]
    
Send 3 messages with message content provided by a file

    ./mesh.sh send [mailbox_id] "@../src/intTest/resources/edifact/pathology.dat" 3

[count] parameter indicates how many messages to send
    
### Download a message

Download a message. Message content is printed to the console. The message id `20200603145356720373_0D25C7` 
is used in this example. Get the message id from the response of the `inbox` or `send` commands.

    ./mesh.sh download [mailbox_id] 20200603145356720373_0D25C7

### Acknowledge a message

Acknowledge a message to remove it from your inbox. The message id `20200603145356720373_0D25C7` 
is used in this example. Get the message id from the response of the `inbox` or `send` commands.

    ./mesh.sh ack [mailbox_id] 20200603145356720373_0D25C7

## Using with Fake MESH

Copy `env.fake-mesh.sh` to `env.sh`
    
Note the value of MAILBOX_ID. This should be changed for the scenario being tested to reflect the mailbox id that the 
application uses to send or receive messages.
