#!/bin/bash

# Find Java installation
if [[ "$(uname)" == "Darwin" ]]; then
    # macOS: Use /usr/libexec/java_home
    JAVA_HOME=$(/usr/libexec/java_home -v 17)
    if [ -z "$JAVA_HOME" ]; then
        echo "Java 17 not found. Please install Java 17."
        exit 1
    fi
else
    # Linux and others
    JAVA_PATH=$(which java)
    if [ -z "$JAVA_PATH" ]; then
        echo "Java not found. Please install Java 17."
        exit 1
    fi

    # Resolve symbolic links
    JAVA_REAL_PATH=$(readlink -f "$JAVA_PATH")
    JAVA_HOME=$(dirname $(dirname "$JAVA_REAL_PATH"))
fi

# Set JAVA_HOME and update PATH
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

echo "JAVA_HOME set to: $JAVA_HOME"

# Optional: Verify Java version
java -version
