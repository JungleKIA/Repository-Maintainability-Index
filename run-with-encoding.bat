@echo off
REM ============================================================================
REM Repository Maintainability Index - UTF-8 Launch Script for Windows
REM ============================================================================
REM This script ensures proper UTF-8 encoding for Unicode character display
REM in Windows Command Prompt, PowerShell, and Git Bash terminals.
REM
REM Usage:
REM   run-with-encoding.bat analyze owner/repo [options]
REM
REM Examples:
REM   run-with-encoding.bat analyze prettier/prettier
REM   run-with-encoding.bat analyze facebook/react --llm
REM   run-with-encoding.bat analyze owner/repo --token YOUR_TOKEN
REM ============================================================================

REM Configure UTF-8 code page for Windows console (65001 = UTF-8)
chcp 65001 > nul 2>&1
if errorlevel 1 (
    echo Warning: Could not set UTF-8 code page. Unicode characters may not display correctly.
)

REM Set JVM encoding properties for UTF-8 support
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dconsole.encoding=UTF-8

REM Check if Maven is available
where mvn >nul 2>&1
if errorlevel 1 (
    echo Error: Maven is not installed or not in PATH.
    echo Please install Maven from https://maven.apache.org/
    exit /b 1
)

REM Check if JAR file exists
if exist "target\repo-maintainability-index-1.0.0.jar" (
    REM Run using JAR file (faster)
    java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -jar target\repo-maintainability-index-1.0.0.jar %*
) else (
    REM Run using Maven (will compile if needed)
    echo JAR file not found. Running with Maven...
    mvn exec:java -Dexec.mainClass="com.kaicode.rmi.Main" -Dexec.args="%*"
)

REM Preserve exit code
exit /b %ERRORLEVEL%
