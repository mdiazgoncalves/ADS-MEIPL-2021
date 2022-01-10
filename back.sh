#/bin/sh

echo "create backend container"
docker container create -p 8080:8080 --restart always --name ads-backend mcdgs/ads-g10-meipl-2021-back:latest
echo "start backend container"
docker container start ads-backend
