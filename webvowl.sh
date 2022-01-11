#/bin/sh

echo "create webvowl container"
docker container create -p 8088:8080 --restart always --name ads-webvowl mcdgs/ads-g10-meipl-2021-webvowl:latest
echo "start webvowl container"
docker container start ads-webvowl
