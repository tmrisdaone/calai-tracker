#!/bin/sh

# Gradle wrapper
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Use the maximum available
JAVA_HOME="/usr/lib/jvm/java-17-openjdk-arm64"
export JAVA_HOME

# Determine the project base dir
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        JAVACMD="java"
    fi
else
    JAVACMD="java"
fi

# Download the wrapper jar if it doesn't exist
if [ ! -f "$CLASSPATH" ] ; then
    echo "Downloading Gradle wrapper JAR..."
    APP_HOME_PARENT=$(dirname "$APP_HOME")
    cd "$APP_HOME_PARENT"
    if command -v curl >/dev/null 2>&1; then
        curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar" -o "$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar" -O "$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
    fi
    cd "$SAVED"
fi

exec "$JAVACMD" \
    $DEFAULT_JVM_OPTS \
    $JAVA_OPTS \
    $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
