# To use any of the optional configuration options uncomment that line. If you no longer want to use an optional
# configuration option then comment that line. Don't use empty string '' or you'll likely to break things

# Default fake-mesh password
export MAILBOX_PASSWORD='password'

# (optional) mailbox is to send messages to (recipient). If not provided all messages are sent to MAILBOX_ID
#export TO_MAILBOX=''

# Shared key used to generate auth token.
export SHARED_KEY='SharedKey'

# hostname and port (not scheme) of the MESH API.
export HOST='localhost:8829'

# path to the file containing the fake-mesh private key.
export MESH_ENDPOINT_PRIVATE_KEY="./fakemesh.ca.key.pem"

# path the file containing the fake-mesh endpoint certificate.
export MESH_ENDPOINT_CERT="./fakemesh.ca.cert.pem"

# (optional) provide different flags / options for the curl command
#export CURL_FLAGS="-s -i -k -o /dev/null"
# WARNING: 'unset CURL_FLAGS' or close terminal immediately after use to avoid
# unintended security risks (due to '-k') in subsequent 'curl' commands.

# WorkflowID used to send MESH messages.
# Can be either PATH_MEDRPT_V3, PATH_MEDRPT_V3_ACK, SCRN_BCS_MEDRPT_V4 or SCRN_BCS_MEDRPT_V4_ACK.
export WORKFLOW_ID=PATH_MEDRPT_V3
