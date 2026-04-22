#include <Adafruit_NeoPixel.h>

namespace {
  constexpr uint8_t LED_PIN = 6;
  constexpr uint8_t LED_COUNT = 4;
  constexpr uint32_t BAUD_RATE = 9600;
}

Adafruit_NeoPixel pixels(LED_COUNT, LED_PIN, NEO_GRB + NEO_KHZ800);
String incomingCommand;

void setup() {
  Serial.begin(BAUD_RATE);
  pixels.begin();
  pixels.clear();
  pixels.show();
}

void loop() {
  while (Serial.available() > 0) {
    char current = static_cast<char>(Serial.read());

    if (current == '\n' || current == '\r') {
      incomingCommand.trim();
      if (incomingCommand.length() > 0) {
        handleCommand(incomingCommand);
        incomingCommand = "";
      }
    } else {
      incomingCommand += current;
    }
  }
}

void handleCommand(String command) {
  command.toUpperCase();

  if (command == "STOP") {
    showSolidColor(255, 0, 0);
  } else if (command == "WAIT") {
    blinkYellow();
  } else if (command == "GO") {
    showSolidColor(0, 255, 0);
  } else if (command == "OFF") {
    showSolidColor(0, 0, 0);
  } else if (command.startsWith("RGB:")) {
    applyRgbCommand(command.substring(4));
  }
}

void showSolidColor(uint8_t red, uint8_t green, uint8_t blue) {
  for (uint8_t i = 0; i < LED_COUNT; i++) {
    pixels.setPixelColor(i, pixels.Color(red, green, blue));
  }
  pixels.show();
}

void blinkYellow() {
  for (uint8_t round = 0; round < 3; round++) {
    showSolidColor(255, 120, 0);
    delay(250);
    showSolidColor(0, 0, 0);
    delay(150);
  }
  showSolidColor(255, 120, 0);
}

void applyRgbCommand(String rgbText) {
  int firstComma = rgbText.indexOf(',');
  int secondComma = rgbText.indexOf(',', firstComma + 1);

  if (firstComma < 0 || secondComma < 0) {
    return;
  }

  int red = rgbText.substring(0, firstComma).toInt();
  int green = rgbText.substring(firstComma + 1, secondComma).toInt();
  int blue = rgbText.substring(secondComma + 1).toInt();

  red = constrain(red, 0, 255);
  green = constrain(green, 0, 255);
  blue = constrain(blue, 0, 255);

  showSolidColor(red, green, blue);
}
