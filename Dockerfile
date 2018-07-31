FROM openjdk:8-jdk-alpine

RUN apk add --update bash

COPY ./target/universal/dist /app

EXPOSE 9000

CMD bash /app/bin/spike-rules-engine -Dhttp.port=9000