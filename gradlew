#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Set default values
DEFAULT_JVM_OPTS=""
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Locate the wrapper properties file
WRAPPER_PROPERTIES_FILE="./gradle/wrapper/gradle-wrapper.properties"

# Read gradle distribution url from wrapper properties
DIST_URL=`sed -n 's/^distributionUrl=//p' $WRAPPER_PROPERTIES_FILE`

# Download and cache Gradle if not present
GRADLE_DIR="$HOME/.gradle/wrapper/dists"
DIST_DIR=`echo "$DIST_URL" | sed -e 's|.*/||' -e 's/\.zip//'`
ZIP_PATH="$GRADLE_DIR/$DIST_DIR/gradle.zip"

# Unzip and run
java -cp "./gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
