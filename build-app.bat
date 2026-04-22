@echo off
setlocal

if not defined JAVA_HOME if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
)

if defined MAVEN_HOME (
    "%MAVEN_HOME%\bin\mvn.cmd" clean package
) else if exist "%USERPROFILE%\Tools\apache-maven-3.9.9\bin\mvn.cmd" (
    "%USERPROFILE%\Tools\apache-maven-3.9.9\bin\mvn.cmd" clean package
) else (
    mvn clean package
)
