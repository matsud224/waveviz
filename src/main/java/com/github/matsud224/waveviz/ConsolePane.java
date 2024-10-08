package com.github.matsud224.waveviz;

import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class ConsolePane extends JPanel {
    private final JTextArea consoleOutputArea;
    private final JTextField consoleInputField;
    private final ScriptingContainer container;

    private final int MAX_HISTORY = 100;
    private final LinkedList<String> lineHistory = new LinkedList<>();
    private ListIterator<String> historyFetchIter;

    private DecoderManager decoderManager = new DecoderManager();

    public class DecoderManager {
        private ArrayList<String> decoders = new ArrayList<>();

        public void registerDecoder(String name) {
            decoders.add(name);
            System.out.printf("Decoder \"%s\" is registered.\n", name);
        }
    }

    public ConsolePane(WaveViewModel model) throws IOException {
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

        consoleOutputArea = new JTextArea();
        consoleOutputArea.setFont(WavevizSettings.CONSOLE_FONT);
        consoleOutputArea.setEditable(false);

        // Initialize JRuby (to make startup time appear shorter)
        try {
            container.put("Model", model);
            container.put("Decoders", decoderManager);
            container.runScriptlet("puts \"*** waveviz Ruby console ***\"");
        } catch (Exception ignored) {
        }
        try {
            container.runScriptlet(PathType.RELATIVE, "src/main/resources/scripts/init.rb");
        } catch (Exception e) {
            consoleOutputArea.append(String.format("Failed to load init.rb: %s\n", e.getMessage()));
        }

        consoleInputField = new JTextField();
        consoleInputField.setFont(WavevizSettings.CONSOLE_FONT);

        consoleInputField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    String cmd = consoleInputField.getText();
                    consoleOutputArea.append("> " + cmd + "\n");

                    try {
                        container.runScriptlet(cmd);
                    } catch (Exception ex) {
                        consoleOutputArea.append(ex.getMessage() + "\n");
                    }

                    doneHistoryFetch();
                    recordHistory(cmd);

                    consoleInputField.setText("");
                    consoleOutputArea.setCaretPosition(consoleOutputArea.getDocument().getLength());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    var nextHistory = getNextHistory();
                    if (nextHistory != null)
                        consoleInputField.setText(nextHistory);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    var previousHistory = getPreviousHistory();
                    if (previousHistory != null)
                        consoleInputField.setText(previousHistory);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        add(new JScrollPane(consoleOutputArea), BorderLayout.CENTER);
        add(consoleInputField, BorderLayout.SOUTH);
    }

    private void recordHistory(String line) {
        if (lineHistory.size() == MAX_HISTORY) {
            lineHistory.removeLast();
        }
        lineHistory.addFirst(line);
    }

    private String getNextHistory() {
        if (historyFetchIter == null)
            historyFetchIter = lineHistory.listIterator();
        return historyFetchIter.hasNext() ? historyFetchIter.next() : null;
    }

    private String getPreviousHistory() {
        if (historyFetchIter == null)
            historyFetchIter = lineHistory.listIterator();
        return historyFetchIter.hasPrevious() ? historyFetchIter.previous() : null;
    }

    private void doneHistoryFetch() {
        historyFetchIter = null;
    }
}
