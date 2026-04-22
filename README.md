# Mini Traffic Signal with Java Swing and Arduino

This project lets a Java Swing desktop app control your Arduino over USB serial and turn your RGB LED module into a mini traffic signal.

## What it does

- `STOP` -> all LEDs turn red
- `WAIT` -> LEDs blink yellow, then stay yellow
- `GO` -> all LEDs turn green
- `OFF` -> LEDs turn off
- `RGB:r,g,b` -> all LEDs switch to a custom color from the desktop sliders

## Hardware used

- Keyestudio Arduino-compatible board
- USB cable
- 4-pixel RGB LED module

## Wiring

Your LED module appears to have 3 pins:

- `G` -> Arduino `GND`
- `V` -> Arduino `5V`
- `S` -> Arduino digital pin `6`

If your module labels are slightly different, connect:

- `G / GND` to `GND`
- `V / VCC / +` to `5V`
- `S / DIN / IN` to `D6`

## Arduino setup

1. Open [arduino/MiniTrafficSignal/MiniTrafficSignal.ino](./arduino/MiniTrafficSignal/MiniTrafficSignal.ino) in the Arduino IDE.
2. Install the `Adafruit NeoPixel` library from Library Manager.
3. Select your Arduino board and COM port.
4. Upload the sketch.

### Arduino CLI helpers

```bat
compile-arduino.bat
upload-arduino.bat COM5
```

## Java setup

This app uses `jSerialComm` for serial communication.

## Desktop app features

- Manual COM port selection
- `Auto Detect` for likely Arduino ports
- Traffic light style visual preview
- Quick buttons for `STOP`, `WAIT`, `GO`, and `OFF`
- Custom RGB sliders for your 4-pixel LED module

### Build with Maven

```bash
mvn clean package
```

Or on Windows:

```bat
build-app.bat
```

The runnable jar will be created at:

`target/traffic-signal-controller-1.0.0-jar-with-dependencies.jar`

### Run

```bash
java -jar target/traffic-signal-controller-1.0.0-jar-with-dependencies.jar
```

Or on Windows:

```bat
run-app.bat
```

## How to use

1. Plug the Arduino into the PC.
2. Upload the Arduino sketch.
3. Start the Java app.
4. Click `Refresh Ports`.
5. Select the Arduino COM port.
6. Click `Connect` or `Auto Detect`.
7. Press `STOP`, `WAIT`, `GO`, or `OFF`.
8. Or adjust the RGB sliders and click `Apply Custom RGB`.

## Project structure

- [pom.xml](./pom.xml)
- [src/main/java/com/example/traffic/TrafficSignalApp.java](./src/main/java/com/example/traffic/TrafficSignalApp.java)
- [src/main/java/com/example/traffic/SerialTrafficController.java](./src/main/java/com/example/traffic/SerialTrafficController.java)
- [arduino/MiniTrafficSignal/MiniTrafficSignal.ino](./arduino/MiniTrafficSignal/MiniTrafficSignal.ino)

## Notes

- Keep the Arduino IDE Serial Monitor closed while the Java app is connected.
- If `WAIT` looks orange instead of yellow, that is normal on many RGB LEDs.
- If nothing lights up, first check the `S`, `5V`, and `GND` wiring.
- If auto-detect does not find the board, refresh the list and choose the correct `COM` port manually.
