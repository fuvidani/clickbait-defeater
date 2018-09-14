FROM openjdk:8-jdk-alpine

ARG VERSION
RUN apk add --no-cache bash
ADD ./build/libs/content-extraction-service-${VERSION}.jar app.jar
ADD entrypoint.sh /entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["sh", "/entrypoint.sh"]