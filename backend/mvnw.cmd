@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "MAVEN_HOME=%MAVEN_HOME%"
if "%MAVEN_HOME%"=="" set "MAVEN_HOME=%USERPROFILE%\apache-maven-3.9.16"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Maven not found at %MAVEN_HOME%. Install it or set MAVEN_HOME.
  exit /b 1
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
