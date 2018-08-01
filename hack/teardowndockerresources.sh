#!/usr/bin/env bash

docker stop goatzilladb
    docker stop goatzillaapp || \
    sleep 3 && \
    docker network rm goatzillanet || \
    true
