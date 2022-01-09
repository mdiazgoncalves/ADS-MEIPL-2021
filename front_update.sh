#!/bin/bash
docker container stop ads-frontend
docker rm ads-frontend
docker rmi mcdgs/ads-g10-meipl-2021-front
docker pull mcdgs/ads-g10-meipl-2021-front:latest
docker container create -p 8088:8080 --restart always --name ads-frontend mcdgs/ads-g10-meipl-2021-front
docker container start ads-frontend
docker image prune -a -f
