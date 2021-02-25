#!/bin/bash 

set -e

source ./version.sh

LIGHT_GREEN='\033[1;32m'
RED='\033[31m'
NC='\033[0m'

echo -e "${LIGHT_GREEN}Exporting environment variables in vars.sh${NC}"
if [ -f "vars.sh" ]; then
    source vars.sh
else
  echo -e "${LIGHT_GREEN}Missing vars.sh file. Using default docker-compose.yaml values${NC}"
fi

echo -e "${LIGHT_GREEN}Stopping running containers${NC}"
docker-compose down

echo -e "${LIGHT_GREEN}Building and starting dependencies${NC}"
docker-compose up -d

if [ "$1" == "-n" ];
then
  echo -e "${RED}Skipping docker image pull for pre-release testing${NC}"
else
  echo -e "${LIGHT_GREEN}Pulling Lab Results adaptor image ${RELEASE_VERSION}${NC}"
  export LAB_RESULTS_IMAGE="nhsdev/nia-lab-results-adaptor:${RELEASE_VERSION}"
  docker pull "$LAB_RESULTS_IMAGE"
fi

echo -e "${LIGHT_GREEN}Starting Lab Results adaptor ${RELEASE_VERSION}${NC}"
docker-compose up -d --no-build lab-results

echo -e "${LIGHT_GREEN}Verify all containers are up${NC}"
docker-compose ps