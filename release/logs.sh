#!/bin/bash 

LIGHT_GREEN='\033[1;32m' 
NC='\033[0m'

echo -e "${LIGHT_GREEN}Following lab-results container logs${NC}"
cd ../ || exit 1
docker-compose logs -f lab-results
