#!/bin/bash

set -ex

LIGHT_GREEN='\033[1;32m' 
NC='\033[0m'

cd ./../../mesh

source env.fake-mesh.sh
echo -e "${LIGHT_GREEN}Sending test edifact message to fake mesh${NC}"
./mesh.sh send lab_results_mailbox "@./../src/intTest/resources/edifact/pathology_3.edifact.dat"
./mesh.sh send lab_results_mailbox "@./../src/intTest/resources/edifact/pathology_3.edifact.dat"
cd ../release/tests
python3 download_message.py amqp://localhost lab_results_gp_outbound
