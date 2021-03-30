#!/usr/bin/env bash

MAILBOX_ID="$2"
TO_MAILBOX=${TO_MAILBOX:-${MAILBOX_ID}}
TOKEN=''
HOST="${HOST}"
MESH_ENDPOINT_PRIVATE_KEY="${MESH_ENDPOINT_PRIVATE_KEY}"
MESH_ENDPOINT_CERT="${MESH_ENDPOINT_CERT}"
CURL_FLAGS="${CURL_FLAGS:--k}" # insecure, disable cert validation for fake-mesh. Add -i for headers
WORKFLOW_ID="${WORKFLOW_ID:PATH_MEDRPT_V3}"

create_token() {
  local nonce
  local nonce_count
  local timestamp
  local hash_content
  local hash_value
  nonce=$(uuidgen)
  nonce=$(echo "${nonce}" | tr '[:upper:]' '[:lower:]')  # to lowercase
  nonce_count='001'
  timestamp=$(date +"%Y%m%d%H%M")
  hash_content="${MAILBOX_ID}:${nonce}:${nonce_count}:${MAILBOX_PASSWORD}:${timestamp}"
  hash_value=$(echo -n "${hash_content}" | openssl dgst -sha256 -hmac "${SHARED_KEY}" | sed 's/^.* //')
  TOKEN="NHSMESH ${MAILBOX_ID}:${nonce}:${nonce_count}:${timestamp}:${hash_value}"
}

authorization() {
  curl ${CURL_FLAGS} -X POST "https://${HOST}/messageexchange/${MAILBOX_ID}" \
    --cert "${MESH_ENDPOINT_CERT}" --key "${MESH_ENDPOINT_PRIVATE_KEY}" -H "Authorization: ${TOKEN}" \
    -H 'Mex-ClientVersion: 1.0' -H 'Mex-JavaVersion: 1.7.0_60' -H 'Mex-OSArchitecture: Windows 7' \
    -H 'Mex-OSName: x86' -H 'Mex-OSVersion: 6.1'
}

inbox() {
  curl ${CURL_FLAGS} -X GET "https://${HOST}/messageexchange/${MAILBOX_ID}/inbox" \
    --cert "${MESH_ENDPOINT_CERT}" --key "${MESH_ENDPOINT_PRIVATE_KEY}" -H "Authorization: ${TOKEN}" \
    -H 'Mex-ClientVersion: 1.0' -H 'Mex-JavaVersion: 1.7.0_60' -H 'Mex-OSArchitecture: Windows 7' \
    -H 'Mex-OSName: x86' -H 'Mex-OSVersion: 6.1'
}

send() {
  local body
  body="$1"
  curl ${CURL_FLAGS} -X POST "https://${HOST}/messageexchange/${MAILBOX_ID}/outbox" \
    --cert "${MESH_ENDPOINT_CERT}" --key "${MESH_ENDPOINT_PRIVATE_KEY}" -H "Authorization: ${TOKEN}" -d "${body}" \
    -H "Mex-From: ${MAILBOX_ID}" -H "Mex-To: ${TO_MAILBOX}" -H "Mex-WorkflowID: ${WORKFLOW_ID}" \
    -H 'Content-Type:application/octet-stream' -H 'Mex-MessageType: DATA'  -H 'Mex-FileName: test-filename.txt' \
    -H 'Mex-Version: 1.0' -H 'Mex-ClientVersion: 1.0' -H 'Mex-JavaVersion: 1.7.0_60' -H 'Mex-OSArchitecture: Windows 7' \
    -H 'Mex-OSName: x86' -H 'Mex-OSVersion: 6.1'
}

download() {
  local message_id
  message_id=$1
  curl ${CURL_FLAGS} -X GET "https://${HOST}/messageexchange/${MAILBOX_ID}/inbox/${message_id}" \
    --cert "${MESH_ENDPOINT_CERT}" --key "${MESH_ENDPOINT_PRIVATE_KEY}" -H "Authorization: ${TOKEN}" \
    -H 'Mex-ClientVersion: 1.0' -H 'Mex-JavaVersion: 1.7.0_60' -H 'Mex-OSArchitecture: Windows 7' \
    -H 'Mex-OSName: x86' -H 'Mex-OSVersion: 6.1'
}

ack() {
  local message_id
  message_id=$1
  curl -i ${CURL_FLAGS} -X PUT "https://${HOST}/messageexchange/${MAILBOX_ID}/inbox/${message_id}/status/acknowledged" \
    --cert "${MESH_ENDPOINT_CERT}" --key "${MESH_ENDPOINT_PRIVATE_KEY}" -H "Authorization: ${TOKEN}" \
    -H 'Mex-ClientVersion: 1.0' -H 'Mex-JavaVersion: 1.7.0_60' -H 'Mex-OSArchitecture: Windows 7' \
    -H 'Mex-OSName: x86' -H 'Mex-OSVersion: 6.1'
}

create_token

if [ "$1" = "auth" ]
then
  authorization
elif [ "$1" = "inbox" ]
then
  inbox
elif [ "$1" = "send" ]
then
  send "$3"
elif [ "$1" = "download" ]
then
  download "$3"
elif [ "$1" = "ack" ]
then
  ack "$3"
fi
