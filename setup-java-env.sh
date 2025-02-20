#!/bin/bash

# Find Java installation
JAVA_PATH=$(which java)
if [ -z "$JAVA_PATH" ]; then
    echo "Java not found. Please install Java 17."
    exit 1
fi

# Resolve symbolic links to get real path
JAVA_REAL_PATH=$(readlink -f "$JAVA_PATH")

# Set JAVA_HOME
export JAVA_HOME=$(dirname $(dirname "$JAVA_REAL_PATH"))

echo "JAVA_HOME set to: $JAVA_HOME"

# Optional: Verify Java version
java -version
