@echo off
setlocal

if not exist "target\traffic-signal-controller-1.0.0-jar-with-dependencies.jar" (
    echo Build output not found. Run build-app.bat first.
    exit /b 1
)

if defined JAVA_HOME (
    "%JAVA_HOME%\bin\java.exe" -jar "target\traffic-signal-controller-1.0.0-jar-with-dependencies.jar"
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin\java.exe" (
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin\java.exe" -jar "target\traffic-signal-controller-1.0.0-jar-with-dependencies.jar"
) else (
    java -jar "target\traffic-signal-controller-1.0.0-jar-with-dependencies.jar"
)
