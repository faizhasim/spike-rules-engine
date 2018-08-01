#!/bin/bash

set +euxo pipefail

{
    curl -o- https://raw.githubusercontent.com/faizhasim/spike-rules-engine/master/hack/teardowndockerresources.sh | bash
    curl -o- https://raw.githubusercontent.com/faizhasim/spike-rules-engine/master/hack/runviadocker.sh | bash
    docker run --rm --name e2erunner  -v `pwd`:/app -w /app --net=goatzillanet -e API_SERVER=goatzillaapp --entrypoint yarn node:10-alpine test
} || {
    curl -o- https://raw.githubusercontent.com/faizhasim/spike-rules-engine/master/hack/teardowndockerresources.sh | bash
}
