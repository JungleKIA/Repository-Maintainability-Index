@echo off
REM Quick launcher for Repository Maintainability Index with UTF-8 support
chcp 65001 > nul 2>&1
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar %*
