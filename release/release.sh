#!/bin/bash 

set -e

source ./version.sh

cd ../
docker-compose build lab-results

docker tag local/lab-results:latest nhsdev/nia-lab-results-adaptor:${RELEASE_VERSION}

docker scan --severity high --file ./Dockerfile --exclude-base nhsdev/nia-lab-results-adaptor:${RELEASE_VERSION}

if [ "$1" == "-y" ];
then
  echo "Tagging and pushing Docker image and git tag"
  docker push nhsdev/nia-lab-results-adaptor:${RELEASE_VERSION}
  git tag -a ${RELEASE_VERSION} -m "Release ${RELEASE_VERSION}"
  git push origin ${RELEASE_VERSION}
fi
