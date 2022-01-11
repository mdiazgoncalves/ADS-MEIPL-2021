#!/bin/bash

echo "stop webvowl container"
docker container stop ads-webvowl
echo "delete webvowl container"
docker rm ads-webvowl
echo "remove webvowl docker image"
docker rmi mcdgs/ads-g10-meipl-2021-webvowl
echo "get latest version webvowl docker image"
docker pull mcdgs/ads-g10-meipl-2021-webvowl:latest
echo "create webvowl container"
docker container create -p 8088:8080 --restart always --name ads-webvowl mcdgs/ads-g10-meipl-2021-webvowl:latest
echo "start webvowl container"
docker container start ads-webvowl
echo "delete unused images"
docker image prune -a -f
