#!/bin/sh

# Gradle start up script for QuantumG

APP_HOME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

export JAVA_HOME=/home/user/jdk-17.0.12+7
JAVACMD="$JAVA_HOME/bin/java"

exec "$JAVACMD" \
    -Xmx2048m \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"