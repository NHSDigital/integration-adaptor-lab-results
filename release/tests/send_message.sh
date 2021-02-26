#!/bin/bash

set -ex

LIGHT_GREEN='\033[1;32m' 
NC='\033[0m'

cd ./../../mesh

echo -e "${LIGHT_GREEN}Loading env variables${NC}"
source env.fake-mesh.sh

echo -e "${LIGHT_GREEN}Sending test edifact message to fake mesh${NC}"
./mesh.sh send gp_mailbox "@./../src/intTest/resources/edifact/pathology.dat"