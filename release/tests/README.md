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
This example bash script will send the test message file located at `/integration-adaptor-lab-results/src/intTest/resources/edifact/pathology_3.edifact.dat` 
to the MESH mailbox `lab_results_ack_mailbox` found in `localhost:8161`.
The MESH client script that runs is called `mesh_connectivity.py` and is located at `/integration-adaptor-lab-results/release/tests`.
After `15` seconds (as inputted in the 4th CLI argument), the script will check the `lab_results_gp_outbound` MESH queue for expected NHSACK messages, if any.

```bash
source /test/integration-adaptor-lab-results/mesh/env.fake-mesh.sh
export CURL_FLAGS="-s -S -k"
cd release/tests
python3 mesh_connectivity.py "../../mesh/" "lab_results_mailbox" "lab_results_ack_mailbox" 15 "../../src/intTest/resources/edifact/pathology_3.edifact.dat"  
```

which, if successful, will produce following output:
```
Cleaning existing matching NHSACK
Sending ACK for message 20210326133229659091_000000142          ## this line won't appear if there are no matching results, as documented on line 8.
Sending EDIFACT
Sleeping for 15 seconds
Checking mailbox for matching NHSACK
Test passed!!! Found 1 matching NHSACK
Sending ACK for message 20210326133339652775_000000144
```

After setting the CURL_FLAGS variable above, always remove it using `unset CURL_FLAGS` when you're done with the specific task.
Alternatively, you can close the terminal and open a new session.
The -k flag allows insecure server connections and poses a security risk if left active.
