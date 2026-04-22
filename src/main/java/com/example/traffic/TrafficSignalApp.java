package com.example.traffic;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TrafficSignalApp {
    private final SerialTrafficController controller = new SerialTrafficController();
    private JLabel statusLabel;
    private JComboBox<SerialTrafficController.PortOption> portComboBox;
    private SignalView signalView;
    private JSlider redSlider;
    private JSlider greenSlider;
    private JSlider blueSlider;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new TrafficSignalApp().createAndShowUi();
        });
    }

    private void createAndShowUi() {
        JFrame frame = new JFrame("Mini Traffic Signal Controller");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(760, 560);
        frame.setMinimumSize(new Dimension(760, 560));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(12, 12));

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(new Color(238, 240, 243));

        JPanel topPanel = createHeaderPanel();
        JPanel connectionPanel = createConnectionPanel();
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        contentPanel.setOpaque(false);

        signalView = new SignalView();
        JPanel visualPanel = createVisualPanel();
        JPanel controlPanel = createControlPanel();

        contentPanel.add(visualPanel);
        contentPanel.add(controlPanel);

        statusLabel = new JLabel("Status: Not connected");
        statusLabel.setBorder(new EmptyBorder(0, 4, 0, 4));

        root.add(topPanel, BorderLayout.NORTH);
        root.add(connectionPanel, BorderLayout.CENTER);
        root.add(contentPanel, BorderLayout.SOUTH);

        frame.add(root, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);

        refreshPorts();
        attemptStartupAutoConnect();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.disconnect();
            }
        });

        frame.setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Java Swing -> Arduino Traffic Signal");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JLabel subheading = new JLabel("Use traffic states or mix your own RGB color for the LED module.");
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subheading.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subheading);
        return panel;
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(10, 10, 10, 10)));

        portComboBox = new JComboBox<>();
        JButton refreshButton = new JButton("Refresh");
        JButton autoButton = new JButton("Auto Detect");
        JButton connectButton = new JButton("Connect");
        JButton disconnectButton = new JButton("Disconnect");

        JPanel leftButtons = new JPanel(new GridLayout(1, 2, 8, 8));
        leftButtons.setOpaque(false);
        leftButtons.add(refreshButton);
        leftButtons.add(autoButton);

        JPanel rightButtons = new JPanel(new GridLayout(1, 2, 8, 8));
        rightButtons.setOpaque(false);
        rightButtons.add(connectButton);
        rightButtons.add(disconnectButton);

        panel.add(leftButtons, BorderLayout.WEST);
        panel.add(portComboBox, BorderLayout.CENTER);
        panel.add(rightButtons, BorderLayout.EAST);

        refreshButton.addActionListener(e -> refreshPorts());
        autoButton.addActionListener(e -> autoConnect());
        connectButton.addActionListener(e -> connectSelectedPort());
        disconnectButton.addActionListener(e -> {
            controller.disconnect();
            setStatus("Disconnected");
        });

        return panel;
    }

    private JPanel createVisualPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel label = new JLabel("Traffic Signal View");
        label.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel hintPanel = new JPanel();
        hintPanel.setOpaque(false);
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.Y_AXIS));
        hintPanel.add(new JLabel("Red = STOP"));
        hintPanel.add(new JLabel("Yellow = WAIT"));
        hintPanel.add(new JLabel("Green = GO"));

        panel.add(label, BorderLayout.NORTH);
        panel.add(signalView, BorderLayout.CENTER);
        panel.add(hintPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(16, 16, 16, 16)));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Controls");
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JPanel quickActions = new JPanel(new GridLayout(2, 2, 10, 10));
        quickActions.setOpaque(false);

        JButton stopButton = createSignalButton("STOP", new Color(198, 40, 40));
        JButton waitButton = createSignalButton("WAIT", new Color(245, 180, 0));
        JButton goButton = createSignalButton("GO", new Color(46, 125, 50));
        JButton offButton = createSignalButton("OFF", new Color(97, 97, 97));

        stopButton.addActionListener(e -> sendTrafficCommand("STOP", SignalState.STOP));
        waitButton.addActionListener(e -> sendTrafficCommand("WAIT", SignalState.WAIT));
        goButton.addActionListener(e -> sendTrafficCommand("GO", SignalState.GO));
        offButton.addActionListener(e -> sendTrafficCommand("OFF", SignalState.OFF));

        quickActions.add(stopButton);
        quickActions.add(waitButton);
        quickActions.add(goButton);
        quickActions.add(offButton);

        top.add(label);
        top.add(Box.createVerticalStrut(12));
        top.add(quickActions);

        JPanel sliders = new JPanel(new GridBagLayout());
        sliders.setOpaque(false);
        sliders.setBorder(new EmptyBorder(12, 0, 0, 0));

        redSlider = createColorSlider();
        greenSlider = createColorSlider();
        blueSlider = createColorSlider();

        addSliderRow(sliders, 0, "Red", redSlider);
        addSliderRow(sliders, 1, "Green", greenSlider);
        addSliderRow(sliders, 2, "Blue", blueSlider);

        JButton applyRgbButton = new JButton("Apply Custom RGB");
        applyRgbButton.addActionListener(e -> applyCustomRgb());

        panel.add(top, BorderLayout.NORTH);
        panel.add(sliders, BorderLayout.CENTER);
        panel.add(applyRgbButton, BorderLayout.SOUTH);
        return panel;
    }

    private void addSliderRow(JPanel panel, int row, String labelText, JSlider slider) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.set(0, 0, 10, 12);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(slider, gbc);
    }

    private JSlider createColorSlider() {
        JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 0, 255, 0);
        slider.setOpaque(false);
        slider.setMajorTickSpacing(85);
        slider.setPaintTicks(true);
        return slider;
    }

    private JButton createSignalButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        return button;
    }

    private void refreshPorts() {
        portComboBox.removeAllItems();
        for (SerialTrafficController.PortOption port : controller.listPorts()) {
            portComboBox.addItem(port);
        }
        setStatus("Ports refreshed");
    }

    private void autoConnect() {
        refreshPorts();
        SerialTrafficController.PortOption port = findPreferredStartupPort();
        if (port == null) {
            showError("Could not auto-detect the Arduino. Select the COM port manually.");
            return;
        }

        portComboBox.setSelectedItem(port);
        connectSelectedPort();
    }

    private void attemptStartupAutoConnect() {
        SerialTrafficController.PortOption port = findPreferredStartupPort();
        if (port == null) {
            setStatus("No Arduino auto-detected yet. Select the COM port and click Connect.");
            return;
        }

        portComboBox.setSelectedItem(port);
        if (controller.connect(port.systemName())) {
            setStatus("Auto-connected to " + port.systemName());
        } else {
            setStatus("Found " + port.systemName() + " but could not connect automatically.");
        }
    }

    private SerialTrafficController.PortOption findPreferredStartupPort() {
        for (int i = 0; i < portComboBox.getItemCount(); i++) {
            SerialTrafficController.PortOption option = portComboBox.getItemAt(i);
            if ("COM5".equalsIgnoreCase(option.systemName())) {
                return option;
            }
        }
        return controller.autoDetectPort().orElse(null);
    }

    private void connectSelectedPort() {
        SerialTrafficController.PortOption selected = (SerialTrafficController.PortOption) portComboBox.getSelectedItem();
        if (selected == null) {
            showError("No COM port selected.");
            return;
        }

        if (controller.connect(selected.systemName())) {
            setStatus("Connected to " + selected.systemName());
        } else {
            showError("Could not connect to " + selected.systemName() + ". Make sure Arduino IDE Serial Monitor is closed.");
        }
    }

    private void sendTrafficCommand(String signal, SignalState state) {
        if (!ensureConnected()) {
            return;
        }

        if (controller.sendCommand(signal)) {
            signalView.setState(state);
            setStatus("Sent command: " + signal);
        } else {
            showError("Failed to send command: " + signal);
        }
    }

    private void applyCustomRgb() {
        if (!ensureConnected()) {
            return;
        }

        int red = redSlider.getValue();
        int green = greenSlider.getValue();
        int blue = blueSlider.getValue();
        String command = "RGB:" + red + "," + green + "," + blue;

        if (controller.sendCommand(command)) {
            signalView.setCustomColor(new Color(red, green, blue));
            setStatus("Sent custom RGB: " + red + ", " + green + ", " + blue);
        } else {
            showError("Failed to send custom RGB command.");
        }
    }

    private boolean ensureConnected() {
        if (controller.isConnected()) {
            return true;
        }
        showError("Connect to the Arduino first.");
        return false;
    }

    private void setStatus(String text) {
        statusLabel.setText("Status: " + text);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Traffic Signal Controller", JOptionPane.ERROR_MESSAGE);
        setStatus(message);
    }

    private enum SignalState {
        STOP,
        WAIT,
        GO,
        OFF,
        CUSTOM
    }

    private static final class SignalView extends JPanel {
        private SignalState state = SignalState.OFF;
        private Color customColor = Color.BLACK;

        private SignalView() {
            setPreferredSize(new Dimension(240, 340));
            setOpaque(false);
        }

        private void setState(SignalState newState) {
            this.state = newState;
            repaint();
        }

        private void setCustomColor(Color color) {
            customColor = color;
            state = SignalState.CUSTOM;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int housingX = 55;
            int housingY = 20;
            int housingWidth = 130;
            int housingHeight = 290;
            g2.setColor(new Color(40, 43, 48));
            g2.fillRoundRect(housingX, housingY, housingWidth, housingHeight, 36, 36);
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(78, 82, 88));
            g2.drawRoundRect(housingX, housingY, housingWidth, housingHeight, 36, 36);

            paintLamp(g2, 80, 45, activeColorFor(SignalState.STOP), "STOP");
            paintLamp(g2, 80, 135, activeColorFor(SignalState.WAIT), "WAIT");
            paintLamp(g2, 80, 225, activeColorFor(SignalState.GO), "GO");

            if (state == SignalState.CUSTOM) {
                g2.setColor(customColor);
                g2.fillRoundRect(20, 320, 200, 18, 12, 12);
                g2.setColor(new Color(50, 50, 50));
                g2.drawString("Custom RGB", 82, 353);
            }

            g2.dispose();
        }

        private void paintLamp(Graphics2D g2, int x, int y, Color color, String label) {
            g2.setColor(new Color(20, 22, 25));
            g2.fillOval(x - 8, y - 8, 96, 96);
            g2.setColor(color);
            g2.fillOval(x, y, 80, 80);
            g2.setColor(new Color(255, 255, 255, 80));
            g2.fillOval(x + 14, y + 12, 28, 20);
            g2.setColor(Color.WHITE);
            g2.drawString(label, x + 24, y + 103);
        }

        private Color activeColorFor(SignalState lamp) {
            if (state == lamp) {
                return switch (lamp) {
                    case STOP -> new Color(224, 55, 55);
                    case WAIT -> new Color(255, 196, 38);
                    case GO -> new Color(64, 181, 73);
                    default -> Color.DARK_GRAY;
                };
            }
            if (state == SignalState.CUSTOM) {
                return customColor;
            }
            return new Color(76, 80, 84);
        }
    }
}
