# spike-rules-engine [![CircleCI](https://circleci.com/gh/faizhasim/spike-rules-engine.svg?style=svg)](https://circleci.com/gh/faizhasim/spike-rules-engine)

![](https://goatsonline.weebly.com/uploads/3/0/8/1/30819681/4368283_orig.jpg)


## Prerequisite

- MySQL 5.6 (tested)
- sbt


## Getting Started

1. Create DB
        
        create database goatzilla DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
        
2. Run

        sbt run
        
        
## Running using the Docker image

        curl -o- https://raw.githubusercontent.com/faizhasim/spike-rules-engine/master/hack/runviadocker.sh | bash
