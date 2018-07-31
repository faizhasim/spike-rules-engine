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
    docker run -d --rm --name goatzillaapp -e DB_FQDN=goatzilladb -p 9000:9000 --net=goatzillanet faizhasim/spike-rules-engine
fi

echo 'Hosted at http://localhost:9000/. Try couple of things from the routes:'
echo
echo '    DELETE  /checkout/items/$id<[0-9]+>'
echo '    GET     /checkout/items/$id<[0-9]+>'
echo
echo '    GET     /customers/:customerId/checkout/summary'
echo '    GET     /customers/:customerId/checkout/items'
echo '    POST    /customers/:customerId/checkout/items/:productId'
echo
echo 'For example:'
echo
echo '    curl -XPOST http://localhost:9000/customers/default/checkout/items/Classic'
echo '    curl -XPOST http://localhost:9000/customers/default/checkout/items/Standout'
echo '    curl -XPOST http://localhost:9000/customers/default/checkout/items/Premium'
echo '    curl http://localhost:9000/customers/default/checkout/items'
echo '    curl http://localhost:9000/customers/default/checkout/summary'
echo
echo '    curl -XPOST http://localhost:9000/customers/UNILEVER/checkout/items/Classic'
echo '    curl -XPOST http://localhost:9000/customers/UNILEVER/checkout/items/Classic'
echo '    curl -XPOST http://localhost:9000/customers/UNILEVER/checkout/items/Classic'
echo '    curl -XPOST http://localhost:9000/customers/UNILEVER/checkout/items/Premium'
echo '    curl http://localhost:9000/customers/UNILEVER/checkout/items'
echo '    curl http://localhost:9000/customers/UNILEVER/checkout/summary'
echo
echo '    curl -XPOST http://localhost:9000/customers/APPLE/checkout/items/Standout'
echo '    curl -XPOST http://localhost:9000/customers/APPLE/checkout/items/Standout'
echo '    curl -XPOST http://localhost:9000/customers/APPLE/checkout/items/Standout'
echo '    curl -XPOST http://localhost:9000/customers/APPLE/checkout/items/Premium'
echo '    curl http://localhost:9000/customers/APPLE/checkout/items'
echo '    curl http://localhost:9000/customers/APPLE/checkout/summary'
echo
echo '    curl -XPOST http://localhost:9000/customers/NIKE/checkout/items/Premium'
echo '    curl -XPOST http://localhost:9000/customers/NIKE/checkout/items/Premium'
echo '    curl -XPOST http://localhost:9000/customers/NIKE/checkout/items/Premium'
echo '    curl -XPOST http://localhost:9000/customers/NIKE/checkout/items/Premium'
echo '    curl http://localhost:9000/customers/NIKE/checkout/items'
echo '    curl http://localhost:9000/customers/NIKE/checkout/summary'
echo