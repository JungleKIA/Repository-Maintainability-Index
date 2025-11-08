#!/bin/bash
# Bash script for running Repository Maintainability Index with UTF-8 support

# Set UTF-8 locale
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

# Run the application with UTF-8 encoding
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar "$@"
