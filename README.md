# spike-rules-engine [![CircleCI](https://circleci.com/gh/faizhasim/spike-rules-engine.svg?style=svg)](https://circleci.com/gh/faizhasim/spike-rules-engine)

![](https://goatsonline.weebly.com/uploads/3/0/8/1/30819681/4368283_orig.jpg)


## Prerequisite

- MySQL 5.6 (tested)
- sbt


## Getting Started

### Running or Developing on your own machine with your own mysql

1. Create DB
        
        create database goatzilla DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
        
2. Run

        sbt run
        
        
### Running using [pre-build Docker image](https://hub.docker.com/r/faizhasim/spike-rules-engine/)

To run:

        curl -o- https://raw.githubusercontent.com/faizhasim/spike-rules-engine/master/hack/runviadocker.sh | bash
        
To tear down resources created by script above:

        curl -o- https://raw.githubusercontent.com/faizhasim/spike-rules-engine/master/hack/teardowndockerresources.sh | bash

## CI/CD

This project is build on [CircleCI](https://circleci.com/gh/faizhasim/spike-rules-engine) based on pipeline in [.circleci/config.yml](./.circleci/config.yml).