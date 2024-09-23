package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class WaveformPanel extends JPanel implements Scrollable, MouseMotionListener, MouseListener, ActionListener, PropertyChangeListener {
    private WaveViewModel model;
    private int pixelsPerUnitTime = 40; // reciprocal if negative
    private final ArrayList<ScaleChangeListener> scaleChangeListeners = new ArrayList<>();
    private JPopupMenu popupMenu;
    private Point popupPosition;
    private HashMap<Waveform.DisplayFormat, JRadioButtonMenuItem> displayFormatMenuMap = new HashMap<>();

    public WaveformPanel(WaveViewModel model) {
        setModel(model);
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

        if (model.getFocusedIndex().isPresent()) {
            g2.setColor(WavevizSettings.WAVE_FOCUSED_BACKGROUND_COLOR);
            g2.fillRect(clipBounds.x, WavevizSettings.WAVE_ROW_HEIGHT * model.getFocusedIndex().get(), clipBounds.width, WavevizSettings.WAVE_ROW_HEIGHT);
        }
        if (model.getSelectedIndex().isPresent()) {
            g2.setColor(WavevizSettings.WAVE_SELECTED_BACKGROUND_COLOR);
            g2.fillRect(clipBounds.x, WavevizSettings.WAVE_ROW_HEIGHT * model.getSelectedIndex().get(), clipBounds.width, WavevizSettings.WAVE_ROW_HEIGHT);
        }
    }

    private int timeFromXCoordinate(int x) {
        if (pixelsPerUnitTime > 0) {
            return Math.round((float) x / pixelsPerUnitTime);
        } else {
            return WavevizUtilities.safeMultiply(x, -pixelsPerUnitTime).orElse(Integer.MAX_VALUE);
        }
    }

    private int xCoordinateFromTime(int t) {
        if (pixelsPerUnitTime > 0) {
            return WavevizUtilities.safeMultiply(t, pixelsPerUnitTime).orElse(Integer.MAX_VALUE);
        } else {
            return t / (-pixelsPerUnitTime);
        }
    }

    private Optional<Integer> safeTimeFromXCoordinate(int x) {
        if (pixelsPerUnitTime > 0) {
            return Optional.of(x / pixelsPerUnitTime);
        } else {
            return WavevizUtilities.safeMultiply(x, -pixelsPerUnitTime);
        }
    }

    private Optional<Integer> safeXCoordinateFromTime(int t) {
        if (pixelsPerUnitTime > 0) {
            return WavevizUtilities.safeMultiply(t, pixelsPerUnitTime);
        } else {
            return Optional.of(t / -pixelsPerUnitTime);
        }
    }

    private void paintMarkers(Graphics2D g2) {
        var clipBounds = g2.getClipBounds();
        for (Marker m : model.getMarkers()) {
            int x = xCoordinateFromTime(m.getTime());
            if (x >= clipBounds.x && x <= clipBounds.x + clipBounds.width) {
                g2.setColor(m.getColor());
                g2.drawLine(x, clipBounds.y, x, clipBounds.y + clipBounds.height);
            }
        }
    }

    private void paintWaves(Graphics2D g2) {
        var clipBounds = g2.getClipBounds();
        int startIndex = clipBounds.y / WavevizSettings.WAVE_ROW_HEIGHT;
        for (int i = startIndex, y = startIndex * WavevizSettings.WAVE_ROW_HEIGHT;
             i < model.getWaveformCount() && y < clipBounds.y + clipBounds.height;
             i++, y += WavevizSettings.WAVE_ROW_HEIGHT) {

            int upperY = y + WavevizSettings.WAVE_Y_PADDING;
            int lowerY = y + WavevizSettings.WAVE_ROW_HEIGHT - WavevizSettings.WAVE_Y_PADDING;

            var wf = model.getWaveform(i);
            var signal = wf.getSignal();
            var store = signal.getValueChangeStore();

            int startTime = timeFromXCoordinate(clipBounds.x);
            int maxTime = model.getMaxTime();
            String prevValue = null;
            for (int t = startTime, x = xCoordinateFromTime(startTime);
                 x < clipBounds.x + clipBounds.width && t <= maxTime; ) {
                TimeRange tr = store.getValue(t);
                int rightX = x + xCoordinateFromTime(tr.getEndTime() - t + 1);
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
                    if (!trimmedStr.isEmpty())
                        g2.drawString(trimmedStr, x + WavevizSettings.WAVE_LABEL_RIGHT_PADDING, y + WavevizSettings.WAVE_Y_PADDING + WavevizSettings.WAVE_FONT_HEIGHT);
                }

                if (pixelsPerUnitTime > 0) {
                    x = rightX;
                    t = tr.getEndTime() + 1;
                } else {
                    if (x == rightX)
                        x += 1;
                    else
                        x = rightX;
                    t = timeFromXCoordinate(x);
                }
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
        paintMarkers(g2);
    }

    private void update() {
        var maxTime = model.getMaxTime();
        var panelSize = new Dimension(safeXCoordinateFromTime(maxTime).orElse(Integer.MAX_VALUE), WavevizSettings.WAVE_ROW_HEIGHT * model.getWaveformCount());
        setPreferredSize(panelSize);
        scaleChangeListeners.forEach(listener -> listener.scaleChanged(pixelsPerUnitTime, panelSize.width));
        revalidate();
        repaint();
    }

    public WaveViewModel getModel() {
        return model;
    }

    public void setModel(WaveViewModel model) {
        this.model = model;

        while (safeXCoordinateFromTime(model.getMaxTime()).isEmpty()) {
            if (!zoomOut()) {
                JOptionPane.showMessageDialog(this,
                        "Waveform is too large to display.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            }
        }

        update();
    }

    public boolean zoomIn() {
        boolean result = true;
        if (pixelsPerUnitTime < 0) {
            if (pixelsPerUnitTime == -1)
                pixelsPerUnitTime = 2;
            else
                pixelsPerUnitTime /= 2;
        } else if (pixelsPerUnitTime > 0) {
            if (pixelsPerUnitTime * 2 <= WavevizSettings.WAVE_MAX_PIXELS_PER_UNIT_TIME)
                pixelsPerUnitTime *= 2;
            else
                result = false;
        }
        update();
        return result;
    }

    public boolean zoomOut() {
        boolean result = true;
        if (pixelsPerUnitTime < 0) {
            var maxTime = model.getMaxTime();
            if (xCoordinateFromTime(maxTime) > WavevizSettings.WAVE_MIN_WHOLE_WIDTH)
                pixelsPerUnitTime *= 2;
            else
                result = false;
        } else if (pixelsPerUnitTime > 0) {
            if (pixelsPerUnitTime == 1)
                pixelsPerUnitTime = -2;
            else
                pixelsPerUnitTime /= 2;
        }
        update();
        return result;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return null;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return Math.max(5, pixelsPerUnitTime);
        } else {
            return WavevizSettings.WAVE_ROW_HEIGHT;
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return Math.max(50, pixelsPerUnitTime * 5);
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
        Container parent = SwingUtilities.getUnwrappedParent(this);
        return parent instanceof JViewport && parent.getHeight() > this.getPreferredSize().height;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int t = timeFromXCoordinate(e.getX());
        if (t <= model.getMaxTime()) {
            model.getCursor().setTime(t);
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
        model.invalidateFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = (int) (popupPosition.getY() / WavevizSettings.WAVE_ROW_HEIGHT);
        if (e.getActionCommand().equals("wave-remove")) {
            model.removeWaveform(index);
        } else if (e.getActionCommand().equals("wave-set-binary-display-format")) {
            model.getWaveform(index).setDisplayFormat(Waveform.DisplayFormat.BINARY);
        } else if (e.getActionCommand().equals("wave-set-hexadecimal-display-format")) {
            model.getWaveform(index).setDisplayFormat(Waveform.DisplayFormat.HEXADECIMAL);
        }
    }

    public void addScaleChangeListener(ScaleChangeListener listener) {
        scaleChangeListeners.add(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        update();
    }

    public void scrollToTime(int t) {
        var x = xCoordinateFromTime(t);
        scrollRectToVisible(new Rectangle(Math.max(x - 10, 0), getY(), 20, 1));
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
                displayFormatMenuMap.get(model.getWaveform(index).getDisplayFormat()).setSelected(true);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
