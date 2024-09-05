package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;

public class WavePanel extends JPanel implements Scrollable, MouseMotionListener, MouseListener, WaveSelectionListener, ActionListener {
    private ArrayList<Waveform> model;
    private int waveUnitWidth = 40;
    private Optional<Integer> focusedIndex = Optional.empty();
    private Optional<Integer> selectedIndex = Optional.empty();
    private ArrayList<WaveSelectionListener> waveSelectionListeners = new ArrayList<>();
    private ArrayList<WaveStatusListener> waveStatusListeners = new ArrayList<>();
    private JPopupMenu popupMenu;
    private Point popupPosition;
    private HashMap<Waveform.DisplayFormat, JRadioButtonMenuItem> displayFormatMenuMap = new HashMap<>();

    public WavePanel() {
        this.model = new ArrayList<>();
        addMouseListener(this);
        addMouseMotionListener(this);

        // Create Popup Menu
        popupMenu = new JPopupMenu();
        var removeMenuItem = new JMenuItem("Remove");
        removeMenuItem.setActionCommand("wave-remove");
        removeMenuItem.addActionListener(this);
        popupMenu.add(removeMenuItem);

        var displayFormatMenu = new JMenu("Display Format");
        var displayFormatButtonGroup = new ButtonGroup();
        var binaryFormatMenuItem = new JRadioButtonMenuItem("Binary");
        binaryFormatMenuItem.setActionCommand("wave-set-binary-display-format");
        binaryFormatMenuItem.addActionListener(this);
        displayFormatButtonGroup.add(binaryFormatMenuItem);
        displayFormatMenu.add(binaryFormatMenuItem);
        displayFormatMenuMap.put(Waveform.DisplayFormat.BINARY, binaryFormatMenuItem);
        var hexadecimalFormatMenuItem = new JRadioButtonMenuItem("Hexadecimal");
        hexadecimalFormatMenuItem.setActionCommand("wave-set-hexadecimal-display-format");
        hexadecimalFormatMenuItem.addActionListener(this);
        displayFormatButtonGroup.add(hexadecimalFormatMenuItem);
        displayFormatMenu.add(hexadecimalFormatMenuItem);
        displayFormatMenuMap.put(Waveform.DisplayFormat.HEXADECIMAL, hexadecimalFormatMenuItem);
        popupMenu.add(displayFormatMenu);

        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(WavevizSettings.WAVE_BACKGROUND_COLOR);
        var clipBounds = g2.getClipBounds();
        g2.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

        if (focusedIndex.isPresent()) {
            g2.setColor(WavevizSettings.WAVE_FOCUSED_BACKGROUND_COLOR);
            g2.fillRect(clipBounds.x, WavevizSettings.WAVE_ROW_HEIGHT * focusedIndex.get(), clipBounds.width, WavevizSettings.WAVE_ROW_HEIGHT);
        }
        if (selectedIndex.isPresent()) {
            g2.setColor(WavevizSettings.WAVE_SELECTED_BACKGROUND_COLOR);
            g2.fillRect(clipBounds.x, WavevizSettings.WAVE_ROW_HEIGHT * selectedIndex.get(), clipBounds.width, WavevizSettings.WAVE_ROW_HEIGHT);
        }
    }

    private void paintWaves(Graphics2D g2) {
        var clipBounds = g2.getClipBounds();
        int startIndex = clipBounds.y / WavevizSettings.WAVE_ROW_HEIGHT;
        for (int i = startIndex, y = startIndex * WavevizSettings.WAVE_ROW_HEIGHT;
             i < model.size() && y < clipBounds.y + clipBounds.height;
             i++, y += WavevizSettings.WAVE_ROW_HEIGHT) {

            int upperY = y + WavevizSettings.WAVE_Y_PADDING;
            int lowerY = y + WavevizSettings.WAVE_ROW_HEIGHT - WavevizSettings.WAVE_Y_PADDING;

            var wf = model.get(i);
            var signal = wf.getSignal();
            var store = signal.getValueChangeStore();

            int startTime = clipBounds.x / waveUnitWidth;
            String prevValue = null;
            for (int t = startTime, x = startTime * waveUnitWidth;
                 x < clipBounds.x + clipBounds.width; ) {
                TimeRange tr = store.getValue(t);
                int rightX = x + waveUnitWidth * (tr.getEndTime() - t + 1);
                if (signal.getSize() == 1) {
                    if (tr.getValue().equals("0")) {
                        g2.setColor(WavevizSettings.WAVE_LINE_COLOR);
                        g2.drawLine(x, lowerY, rightX, lowerY);
                        if (prevValue != null && prevValue.equals("1")) {
                            g2.drawLine(x, upperY, x, lowerY);
                        }
                    } else if (tr.getValue().equals("1")) {
                        g2.setColor(WavevizSettings.WAVE_HIGH_VALUE_FILL_COLOR);
                        g2.fillRect(x, upperY, rightX - x, lowerY - upperY);
                        g2.setColor(WavevizSettings.WAVE_LINE_COLOR);
                        g2.drawLine(x, upperY, rightX, upperY);
                        if (prevValue != null && prevValue.equals("0")) {
                            g2.drawLine(x, upperY, x, lowerY);
                        }
                    } else {
                        if (tr.getValue().equals("x"))
                            g2.setColor(Color.red);
                        else
                            g2.setColor(Color.yellow);
                        g2.drawLine(x, y + WavevizSettings.WAVE_ROW_HEIGHT / 2, rightX, y + WavevizSettings.WAVE_ROW_HEIGHT / 2);
                    }
                } else {
                    g2.setColor(WavevizSettings.WAVE_LINE_COLOR);
                    g2.drawLine(x, upperY, rightX, upperY);
                    g2.drawLine(rightX, upperY, rightX, lowerY);
                    g2.drawLine(x, lowerY, rightX, lowerY);

                    g2.setColor(WavevizSettings.WAVE_TEXT_COLOR);
                    var metrics = g2.getFontMetrics();
                    var origStr = tr.getValue();
                    var formattedStr = "";
                    switch (wf.getDisplayFormat()) {
                        case BINARY:
                            formattedStr = origStr;
                            break;
                        case HEXADECIMAL:
                            formattedStr = WavevizUtilities.convertVerilogBinaryToHex(origStr);
                            break;
                    }
                    var trimmedStr = WavevizUtilities.getTextWithinWidth(metrics, formattedStr, "..", rightX - x - WavevizSettings.WAVE_LABEL_RIGHT_PADDING * 2);
                    g2.drawString(trimmedStr, x + WavevizSettings.WAVE_LABEL_RIGHT_PADDING, y + WavevizSettings.WAVE_Y_PADDING + WavevizSettings.WAVE_FONT_HEIGHT);
                }

                x = rightX;
                t = tr.getEndTime() + 1;
                prevValue = tr.getValue();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(WavevizSettings.WAVE_MONOSPACE_FONT);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintBackground(g2);
        paintWaves(g2);
    }

    private void update() {
        var maxTime = model.stream().map(wf -> wf.getSignal().getValueChangeStore().getLastTime()).max(Comparator.naturalOrder()).orElse(0);
        setPreferredSize(new Dimension(maxTime * waveUnitWidth, WavevizSettings.WAVE_ROW_HEIGHT * model.size()));
        revalidate();
        repaint();
    }

    public ArrayList<Waveform> getModel() {
        return model;
    }

    public void setModel(ArrayList<Waveform> model) {
        this.model = model;
        selectedIndex = Optional.empty();
        update();
    }

    public void zoomIn() {
        if (waveUnitWidth < 10000)
            waveUnitWidth = waveUnitWidth * 2;
        update();
    }

    public void zoomOut() {
        waveUnitWidth = waveUnitWidth / 2;
        if (waveUnitWidth <= 0)
            waveUnitWidth = 1;
        update();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return null;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return waveUnitWidth;
        } else {
            return WavevizSettings.WAVE_ROW_HEIGHT;
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return waveUnitWidth * 5;
        } else {
            return WavevizSettings.WAVE_ROW_HEIGHT * 5;
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        var index = e.getY() / WavevizSettings.WAVE_ROW_HEIGHT;
        Optional<Integer> newFocusedIndex;
        if (index < model.size()) {
            newFocusedIndex = Optional.of(index);
        } else {
            newFocusedIndex = Optional.empty();
        }
        if (!focusedIndex.equals(newFocusedIndex)) {
            waveSelectionListeners.forEach(listener -> listener.waveFocusChanged(newFocusedIndex));
            focusedIndex = newFocusedIndex;
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        var index = e.getY() / WavevizSettings.WAVE_ROW_HEIGHT;
        Optional<Integer> newSelectedIndex;
        if (index < model.size()) {
            newSelectedIndex = Optional.of(index);
        } else {
            newSelectedIndex = Optional.empty();
        }
        if (!selectedIndex.equals(newSelectedIndex)) {
            waveSelectionListeners.forEach(listener -> listener.waveSelectionChanged(newSelectedIndex));
            selectedIndex = newSelectedIndex;
            repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        focusedIndex = Optional.empty();
        waveSelectionListeners.forEach(listener -> listener.waveFocusChanged(Optional.empty()));
        repaint();
    }

    @Override
    public void waveFocusChanged(Optional<Integer> index) {
        focusedIndex = index;
        repaint();
    }

    @Override
    public void waveSelectionChanged(Optional<Integer> index) {
        selectedIndex = index;
        repaint();
    }

    public void addWaveSelectionListener(WaveSelectionListener listener) {
        waveSelectionListeners.add(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "wave-remove") {
            waveStatusListeners.forEach(listener -> listener.waveRemoved((int) popupPosition.getY() / WavevizSettings.WAVE_ROW_HEIGHT));
        } else if (e.getActionCommand() == "wave-set-binary-display-format") {
            int index = (int) popupPosition.getY() / WavevizSettings.WAVE_ROW_HEIGHT;
            model.get(index).setDisplayFormat(Waveform.DisplayFormat.BINARY);
            waveStatusListeners.forEach(listener -> listener.waveStatusChanged((int) popupPosition.getY() / WavevizSettings.WAVE_ROW_HEIGHT));
        } else if (e.getActionCommand() == "wave-set-hexadecimal-display-format") {
            int index = (int) popupPosition.getY() / WavevizSettings.WAVE_ROW_HEIGHT;
            model.get(index).setDisplayFormat(Waveform.DisplayFormat.HEXADECIMAL);
            waveStatusListeners.forEach(listener -> listener.waveStatusChanged((int) popupPosition.getY() / WavevizSettings.WAVE_ROW_HEIGHT));
        }
    }

    public void addWaveStatusListener(WaveStatusListener listener) {
        waveStatusListeners.add(listener);
    }

    private class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupPosition = e.getPoint();
                var index = (int) popupPosition.getY() / WavevizSettings.WAVE_ROW_HEIGHT;
                displayFormatMenuMap.get(model.get(index).getDisplayFormat()).setSelected(true);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
