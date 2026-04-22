@echo off
setlocal

set "ARDUINO_CLI=%LOCALAPPDATA%\Programs\Arduino IDE\resources\app\lib\backend\resources\arduino-cli.exe"

if not exist "%ARDUINO_CLI%" (
    echo arduino-cli not found at "%ARDUINO_CLI%".
    exit /b 1
)

"%ARDUINO_CLI%" compile --fqbn arduino:avr:mega "%~dp0arduino\MiniTrafficSignal"
