#!/usr/bin/env bash

if ! docker network inspect goatzillanet; then
    docker network create goatzillanet
fi

if ! docker inspect goatzilladb; then
    docker run -d --rm --name goatzilladb -e MYSQL_ALLOW_EMPTY_PASSWORD=true --net=goatzillanet mariadb:10
fi

while ! docker exec goatzilladb mysql -e 'SELECT 1;'; do
    echo Waiting for database to be ready.
    sleep 1
done

docker exec goatzilladb mysql -e 'CREATE DATABASE IF NOT EXISTS goatzilla DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;'

docker pull faizhasim/spike-rules-engine
if ! docker inspect goatzillaapp; then
    docker run --rm --name goatzillaapp -e DB_FQDN=goatzilladb --net=goatzillanet faizhasim/spike-rules-engine
fi