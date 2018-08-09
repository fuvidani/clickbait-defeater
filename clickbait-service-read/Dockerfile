FROM openjdk:8-jdk-alpine

ARG VERSION
ADD ./build/libs/clickbait-service-read-${VERSION}.jar app.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]

EXPOSE 8080