#!/bin/bash
# ============================================================================
# Repository Maintainability Index - UTF-8 Launch Script for Unix/Linux/macOS
# ============================================================================
# This script ensures proper UTF-8 encoding for Unicode character display
# in Unix-based terminals.
#
# Usage:
#   ./run-with-encoding.sh analyze owner/repo [options]
#
# Examples:
#   ./run-with-encoding.sh analyze prettier/prettier
#   ./run-with-encoding.sh analyze facebook/react --llm
#   ./run-with-encoding.sh analyze owner/repo --token YOUR_TOKEN
# ============================================================================

# Configure UTF-8 locale
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export LC_CTYPE=en_US.UTF-8

# Set JVM encoding properties for UTF-8 support
export MAVEN_OPTS="-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dconsole.encoding=UTF-8"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH."
    echo "Please install Maven from https://maven.apache.org/"
    exit 1
fi

# Check if JAR file exists
if [ -f "target/repo-maintainability-index-1.0.0.jar" ]; then
    # Run using JAR file (faster)
    java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar "$@"
else
    # Run using Maven (will compile if needed)
    echo "JAR file not found. Running with Maven..."
    mvn exec:java -Dexec.mainClass="com.kaicode.rmi.Main" -Dexec.args="$*"
fi

# Preserve exit code
exit $?
