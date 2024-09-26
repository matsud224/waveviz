package com.github.matsud224.waveviz;

import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

public class ConsolePane extends JPanel {
    private final JTextArea consoleOutputArea;
    private final JTextField consoleInputField;
    private final ScriptingContainer container;

    public ConsolePane() throws IOException {
        super(new BorderLayout());

        container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);

        PipedOutputStream sender = new PipedOutputStream();
        PipedInputStream receiver = new PipedInputStream(sender);
        PrintStream rubyOutStream = new PrintStream(sender);
        InputStreamReader receiverReader = new InputStreamReader(receiver);
        container.setOutput(rubyOutStream);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        int newChar = receiverReader.read();
                        if (newChar == -1) {
                            SwingUtilities.invokeLater(() -> consoleOutputArea.append("--- closed ---"));
                            return;
                        } else {
                            SwingUtilities.invokeLater(() -> consoleOutputArea.append(Character.toString(newChar)));
                        }
                    } catch (IOException ignored) {
                    }
                }
            }
        }).start();

        // Initialize JRuby (to make startup time appear shorter)
        try {
            container.runScriptlet("puts \"*** waveviz Ruby console ***\"");
        } catch (Exception ignored) {
        }

        consoleOutputArea = new JTextArea();
        consoleOutputArea.setFont(WavevizSettings.CONSOLE_FONT);
        consoleOutputArea.setEditable(false);

        consoleInputField = new JTextField();
        consoleInputField.setFont(WavevizSettings.CONSOLE_FONT);

        consoleInputField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == '\n') {
                    String cmd = consoleInputField.getText();
                    consoleOutputArea.append("> " + cmd + "\n");

                    try {
                        container.runScriptlet(cmd);
                    } catch (Exception ex) {
                        consoleOutputArea.append(ex.getMessage() + "\n");
                    }

                    consoleInputField.setText("");
                    consoleOutputArea.setCaretPosition(consoleOutputArea.getDocument().getLength());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        add(new JScrollPane(consoleOutputArea), BorderLayout.CENTER);
        add(consoleInputField, BorderLayout.SOUTH);
    }
}
