# MESH Connectivity Test Tool

The purpose of this tool is to enable automated testing of Lab Results adaptor's MESH connectivity.

## Overview

The tool performs the following actions:
1) Sends infrastructure ACK for any NHSACK that already exists in MESH that matches 
sender, recipient, interchange sequence number and message sequence numbers of a given test EDIFACT file.
2) Sends given EDIFACT test file to MESH mailbox
3) Waits defined amount of time (for the adaptor to scan MESH, process the message and send NHSACK back)
4) Checks if NHSACK with expected details is present on MESH mailbox
5) Sends infrastructure ACK for matching NHSACKs

## Running the tool
Python 3.8 required.

Set environment variables as described in the [MESH Client README](../../mesh/README.md)
but with the `CURL_FLAGS` set to `"-s -S -k"`.

Run the script with required parameters.

### Example
In the following example, git repository has been cloned to `/test/integration-adaptor-lab-results` folder.

This example will send a test file located at `/test/integration-adaptor-lab-results/src/intTest/resources/edifact/pathology_3.edifact.dat` to MESH mailbox `lab_results_mailbox`
using MESH client script located at `/test/integration-adaptor-lab-results/mesh/` and after `15` seconds, 
it will check `lab_results_ack_mailbox` MESH mailbox for expected NHSACK presence.

```bash
source /test/integration-adaptor-lab-results/mesh/env.fake-mesh.sh
export CURL_FLAGS="-s -S -k"
python3 mesh_connectivity.py "/test/integration-adaptor-lab-results/mesh/" "lab_results_mailbox" "lab_results_ack_mailbox" 15 "/test/integration-adaptor-lab-results/src/intTest/resources/edifact/pathology_3.edifact.dat" 
```

which will produce following output:
```
Cleaning existing matching NHSACK
Sending ACK for message 20210326133229659091_000000142
Sending EDIFACT
Sleeping for 15 seconds
Checking mailbox for matching NHSACK
Test passed!!! Found 1 matching NHSACK
Sending ACK for message 20210326133339652775_000000144
```
