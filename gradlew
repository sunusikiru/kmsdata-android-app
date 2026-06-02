#!/bin/bash

# Gradle wrapper script
GRADLE_VERSION=8.0

if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    echo "Downloading Gradle wrapper..."
    mkdir -p gradle/wrapper
    curl -L -o gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/v$GRADLE_VERSION/gradle/wrapper/gradle-wrapper.jar
fi

exec java -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain "$@"
