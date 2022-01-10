#!/bin/bash

echo "stop frontend container"
docker container stop ads-frontend
echo "delete frontend container"
docker rm ads-frontend
echo "remove frontend docker image"
docker rmi mcdgs/ads-g10-meipl-2021-front
echo "get latest version frontend docker image"
docker pull mcdgs/ads-g10-meipl-2021-front:latest
echo "create frontend container"
docker container create -p 8081:8080 --restart always --name ads-frontend mcdgs/ads-g10-meipl-2021-front:latest
echo "start frontend container"
docker container start ads-frontend
echo "delete unused images"
docker image prune -a -f
