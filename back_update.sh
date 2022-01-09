#!/bin/bash
docker container stop ads-backend
docker rm ads-backend
docker rmi mcdgs/ads-g10-meipl-2021-back
docker pull mcdgs/ads-g10-meipl-2021-back:latest
docker container create -p 8080:8080 --restart always --name ads-backend mcdgs/ads-g10-meipl-2021-back
docker container start ads-backend
docker image prune -a -f
