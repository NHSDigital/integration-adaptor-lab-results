#!/bin/bash 

LIGHT_GREEN='\033[1;32m' 
NC='\033[0m'

echo -e "${LIGHT_GREEN}Stopping lab-results containers${NC}"
cd ../ || exit 1
docker-compose stop
