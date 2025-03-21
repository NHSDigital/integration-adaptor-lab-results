#!/bin/bash

set -ex

LIGHT_GREEN='\033[1;32m' 
NC='\033[0m'

cd ./../../mesh

# Example: sh send_message.sh inbox lab_results_mailbox

echo -e "${LIGHT_GREEN}Sending test edifact message to fake mesh${NC}"
./mesh.sh "$1" "$2" "@./../src/intTest/resources/edifact/pathology_3.edifact.dat"
