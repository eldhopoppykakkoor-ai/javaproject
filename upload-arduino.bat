@echo off
setlocal

set "ARDUINO_CLI=%LOCALAPPDATA%\Programs\Arduino IDE\resources\app\lib\backend\resources\arduino-cli.exe"
set "PORT=%~1"

if "%PORT%"=="" set "PORT=COM5"

if not exist "%ARDUINO_CLI%" (
    echo arduino-cli not found at "%ARDUINO_CLI%".
    exit /b 1
)

"%ARDUINO_CLI%" upload -p %PORT% --fqbn arduino:avr:mega "%~dp0arduino\MiniTrafficSignal"
