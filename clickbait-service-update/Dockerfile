FROM java:8-jre

ARG VERSION
ADD ./build/libs/clickbait-service-update-${VERSION}.jar app.jar
CMD ["java", "-Dspring.profiles.active=prod", "-Djava.security.egd=file:/dev/./urandom", "-Xmx2048m", "-jar", "/app.jar"]

EXPOSE 8000