#!/bin/sh
# Gradle wrapper script generated for GitHub Actions environment

# Attempt to locate gradle or run with system java
CLASSPATH=""
GRADLE_OPTS=""

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    # Fallback to installing or invoking gradle via SDKMAN or wrapper
    echo "Running Gradle command: $@"
    gradle "$@"
fi
