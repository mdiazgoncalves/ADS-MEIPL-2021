#!/bin/bash

echo "stop backend container"
docker container stop ads-backend
echo "delete backend container"
docker rm ads-backend
echo "remove backend docker image"
docker rmi mcdgs/ads-g10-meipl-2021-back
echo "get latest version backend docker image"
docker pull mcdgs/ads-g10-meipl-2021-back:latest
echo "create backend container"
docker container create -p 8080:8080 --restart always --name ads-backend mcdgs/ads-g10-meipl-2021-back:latest
echo "start backend container"
docker container start ads-backend
echo "delete unused images"
docker image prune -a -f
