FROM openjdk:8-jdk-alpine

ARG VERSION
ADD ./build/libs/gateway-${VERSION}.jar app.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]

EXPOSE 4000