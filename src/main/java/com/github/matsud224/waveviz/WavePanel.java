package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class WavePanel extends JPanel implements Scrollable {
    private ArrayList<Signal> model;
    private int waveUnitWidth = 40;

    public WavePanel() {
        this.model = new ArrayList<>();
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(WavevizSettings.WAVE_BACKGROUND_COLOR);
        var clipBounds = g2.getClipBounds();
        g2.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
    }

    private void paintWaves(Graphics2D g2) {
        var clipBounds = g2.getClipBounds();
        int startIndex = clipBounds.y / WavevizSettings.WAVE_ROW_HEIGHT;
        for (int i = startIndex, y = startIndex * WavevizSettings.WAVE_ROW_HEIGHT;
             i < model.size() && y < clipBounds.y + clipBounds.height;
             i++, y += WavevizSettings.WAVE_ROW_HEIGHT) {

            int upperY = y + WavevizSettings.WAVE_Y_PADDING;
            int lowerY = y + WavevizSettings.WAVE_ROW_HEIGHT - WavevizSettings.WAVE_Y_PADDING;

            var signal = model.get(i);
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
                    var valStr = WavevizUtilities.getTextWithinWidth(metrics, tr.getValue(), "..", rightX - x - WavevizSettings.WAVE_LABEL_RIGHT_PADDING * 2);
                    g2.drawString(valStr, x + WavevizSettings.WAVE_LABEL_RIGHT_PADDING, y + WavevizSettings.WAVE_Y_PADDING + WavevizSettings.WAVE_FONT_HEIGHT);
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
        var maxTime = model.stream().map(s -> s.getValueChangeStore().getLastTime()).max(Comparator.naturalOrder()).orElse(0);
        setPreferredSize(new Dimension(maxTime * waveUnitWidth, WavevizSettings.WAVE_ROW_HEIGHT * model.size()));
        revalidate();
        repaint();
    }

    public ArrayList<Signal> getModel() {
        return model;
    }

    public void setModel(ArrayList<Signal> model) {
        this.model = model;
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
}
