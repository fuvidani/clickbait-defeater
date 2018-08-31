#!/usr/bin/env bash

set -e

JAVA_OPTS=${JAVA_OPTS:="-Djava.security.egd=file:/dev/./urandom -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"}

exec java -jar $JAVA_OPTS /app.jar