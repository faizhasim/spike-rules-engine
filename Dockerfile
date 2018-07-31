FROM openjdk:8-jdk-alpine

RUN apk add --update bash

COPY ./target/universal/dist /app

EXPOSE 8080

CMD bash /app/bin/spike-rules-engine -Dhttp.port=8080