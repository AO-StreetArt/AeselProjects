################################################################

# Dockerfile to build Kelona Container Images

################################################################


FROM openjdk:8-jdk-alpine
MAINTAINER Alex Barry
VOLUME /tmp
ADD build/libs/aeselprojects-0.0.1-SNAPSHOT.jar app.jar
ADD src/main/resources/log4j2.yaml log4j2.yaml
ADD src/main/resources/application.properties bootstrap.properties
ADD src/main/resources/application.properties application.properties
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar
