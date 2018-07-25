FROM java:8-jre

ARG VERSION
ADD ./build/libs/gateway-${VERSION}.jar app.jar
CMD ["java", "-Dspring.profiles.active=prod", "-Djava.security.egd=file:/dev/./urandom", "-Xmx2048m", "-jar", "/app.jar"]

EXPOSE 4000