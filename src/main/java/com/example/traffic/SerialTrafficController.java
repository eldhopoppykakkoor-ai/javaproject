package com.example.traffic;

import com.fazecast.jSerialComm.SerialPort;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class SerialTrafficController {
    public record PortOption(String systemName, String displayName) {
        @Override
        public String toString() {
            return displayName;
        }
    }

    private SerialPort activePort;

    public PortOption[] listPorts() {
        return Arrays.stream(SerialPort.getCommPorts())
                .sorted(Comparator.comparing(SerialPort::getSystemPortName, String.CASE_INSENSITIVE_ORDER))
                .map(port -> new PortOption(
                        port.getSystemPortName(),
                        port.getSystemPortName() + " - " + port.getDescriptivePortName()))
                .toArray(PortOption[]::new);
    }

    public Optional<PortOption> autoDetectPort() {
        PortOption[] ports = listPorts();
        if (ports.length == 0) {
            return Optional.empty();
        }

        return Arrays.stream(ports)
                .filter(port -> isLikelyArduino(port.displayName()))
                .findFirst()
                .or(() -> ports.length == 1 ? Optional.of(ports[0]) : Optional.empty());
    }

    public boolean connect(String portName) {
        disconnect();

        Optional<SerialPort> match = Arrays.stream(SerialPort.getCommPorts())
                .filter(port -> port.getSystemPortName().equalsIgnoreCase(portName))
                .findFirst();

        if (match.isEmpty()) {
            return false;
        }

        SerialPort port = match.get();
        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 1000, 1000);

        if (!port.openPort()) {
            return false;
        }

        activePort = port;

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return true;
    }

    public void disconnect() {
        if (activePort != null) {
            activePort.closePort();
            activePort = null;
        }
    }

    public boolean isConnected() {
        return activePort != null && activePort.isOpen();
    }

    public boolean sendCommand(String command) {
        if (!isConnected()) {
            return false;
        }

        byte[] payload = (command.trim() + "\n").getBytes(StandardCharsets.UTF_8);
        return activePort.writeBytes(payload, payload.length) == payload.length;
    }

    private boolean isLikelyArduino(String text) {
        String lower = text.toLowerCase();
        return lower.contains("arduino")
                || lower.contains("wch")
                || lower.contains("ch340")
                || lower.contains("usb serial")
                || lower.contains("cp210")
                || lower.contains("mega 2560");
    }
}
