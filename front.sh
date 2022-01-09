#/bin/sh

docker container create -p 8088:8080 --restart always --name ads-frontend mcdgs/ads-g10-meipl-2021-front
