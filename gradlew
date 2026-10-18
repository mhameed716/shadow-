#!/usr/bin/env sh
#
# Gradle start up script for UN*X - generated to work even without wrapper jar
# If wrapper jar is present, it will be used. Otherwise falls back to system gradle.
#
# This script checks for gradle/wrapper/gradle-wrapper.jar
# and executes it, or delegates to `gradle` command.

set -e

# Find java
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
        exit 1
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || { echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2; exit 1; }
fi

APP_HOME=$(cd "$(dirname "$0")" && pwd)

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ -f "$WRAPPER_JAR" ]; then
    # Use wrapper jar if present
    exec "$JAVACMD" -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
else
    # Fallback: use system gradle (installed via sdkman, brew, or setup-gradle action)
    if command -v gradle >/dev/null 2>&1; then
        echo "Wrapper jar not found - using system gradle. To generate wrapper, run: gradle wrapper --gradle-version 8.4" >&2
        exec gradle "$@"
    else
        echo "ERROR: gradle/wrapper/gradle-wrapper.jar not found and 'gradle' not in PATH." >&2
        echo "Please generate wrapper locally: gradle wrapper --gradle-version 8.4" >&2
        echo "Or install Gradle 8.4 and run: gradle assembleDebug" >&2
        exit 1
    fi
fi
