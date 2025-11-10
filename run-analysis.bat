@echo off
REM Windows Batch script for running Repository Maintainability Index with UTF-8 support

REM Set console to UTF-8 code page
chcp 65001 > nul

REM Run the application with UTF-8 encoding
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar %*

REM Return to original code page
chcp 437 > nul
