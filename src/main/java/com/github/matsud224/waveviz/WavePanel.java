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
        var r = g2.getClipBounds();
        g2.fillRect(r.x, r.y, r.width, r.height);
    }

    private int paintTimeBar(Graphics2D g2, int currentY) {
        int view_start_time = g2.getClipBounds().x / waveUnitWidth;

        int upperY = currentY + WavevizSettings.WAVE_Y_PADDING;
        int lowerY = currentY + WavevizSettings.WAVE_ROW_HEIGHT - WavevizSettings.WAVE_Y_PADDING;

        int timeLabelInterval = 100;

        int currentTime = (view_start_time + (timeLabelInterval - 1)) / timeLabelInterval * timeLabelInterval;
        int currentX = (currentTime - view_start_time) * waveUnitWidth;

        while (currentX < g2.getClipBounds().width && currentTime < 10000) {
            int rightX = currentX + timeLabelInterval * waveUnitWidth;

            g2.setColor(WavevizSettings.WAVE_TEXT_COLOR);
            var metrics = g2.getFontMetrics();
            var labelStr = getTextWithinWidth(metrics, String.format("%d ns", currentTime), "..", rightX - currentX - WavevizSettings.WAVE_LABEL_RIGHT_PADDING * 2);
            g2.drawString(labelStr, currentX + WavevizSettings.WAVE_LABEL_RIGHT_PADDING, currentY + WavevizSettings.WAVE_Y_PADDING + WavevizSettings.WAVE_FONT_HEIGHT);

            g2.setColor(WavevizSettings.TIMEBAR_LINE_COLOR);
            g2.drawLine(currentX, (lowerY - upperY) / 2, currentX, lowerY);

            currentX = rightX;
            currentTime += timeLabelInterval;
        }

        g2.setColor(WavevizSettings.TIMEBAR_LINE_COLOR);
        g2.drawLine(g2.getClipBounds().x, lowerY, g2.getClipBounds().x + g2.getClipBounds().width, lowerY);

        return currentY + WavevizSettings.WAVE_ROW_HEIGHT;
    }

    private void paintWaves(Graphics2D g2) {
        int currentY = 0;

        for (int j = g2.getClipBounds().x / waveUnitWidth; j < model.size(); j++) {
            var signal = model.get(j);
            var store = signal.getValueChangeStore();
            int view_start_time = g2.getClipBounds().x;

            int currentX = 0;
            int currentTime = view_start_time;

            int upperY = currentY + WavevizSettings.WAVE_Y_PADDING;
            int lowerY = currentY + WavevizSettings.WAVE_ROW_HEIGHT - WavevizSettings.WAVE_Y_PADDING;
            String prevValue = null;

            while (currentX < g2.getClipBounds().width && currentTime < store.getLastTime()) {
                TimeRange tr = store.getValue(currentTime);
                int rightX = currentX + waveUnitWidth * (tr.getEndTime() - currentTime + 1);
                if (signal.getSize() == 1) {
                    if (tr.getValue().equals("0")) {
                        g2.setColor(WavevizSettings.WAVE_LINE_COLOR);
                        g2.drawLine(currentX, lowerY, rightX, lowerY);
                        if (prevValue != null && prevValue.equals("1")) {
                            g2.drawLine(currentX, upperY, currentX, lowerY);
                        }
                    } else if (tr.getValue().equals("1")) {
                        g2.setColor(WavevizSettings.WAVE_HIGH_VALUE_FILL_COLOR);
                        g2.fillRect(currentX, upperY, rightX - currentX, lowerY - upperY);
                        g2.setColor(WavevizSettings.WAVE_LINE_COLOR);
                        g2.drawLine(currentX, upperY, rightX, upperY);
                        if (prevValue != null && prevValue.equals("0")) {
                            g2.drawLine(currentX, upperY, currentX, lowerY);
                        }
                    } else {
                        if (tr.getValue().equals("x"))
                            g2.setColor(Color.red);
                        else
                            g2.setColor(Color.yellow);
                        g2.drawLine(currentX, currentY + WavevizSettings.WAVE_ROW_HEIGHT / 2, rightX, currentY + WavevizSettings.WAVE_ROW_HEIGHT / 2);
                    }
                } else {
                    g2.setColor(WavevizSettings.WAVE_LINE_COLOR);
                    g2.drawLine(currentX, upperY, rightX, upperY);
                    g2.drawLine(rightX, upperY, rightX, lowerY);
                    g2.drawLine(currentX, lowerY, rightX, lowerY);

                    g2.setColor(WavevizSettings.WAVE_TEXT_COLOR);
                    var metrics = g2.getFontMetrics();
                    var valStr = getTextWithinWidth(metrics, tr.getValue(), "..", rightX - currentX - WavevizSettings.WAVE_LABEL_RIGHT_PADDING * 2);
                    g2.drawString(valStr, currentX + WavevizSettings.WAVE_LABEL_RIGHT_PADDING, currentY + WavevizSettings.WAVE_Y_PADDING + WavevizSettings.WAVE_FONT_HEIGHT);
                }

                currentX = rightX;
                currentTime = tr.getEndTime() + 1;
                prevValue = tr.getValue();
            }
            currentY += WavevizSettings.WAVE_ROW_HEIGHT;
        }
    }

    private String getTextWithinWidth(FontMetrics metrics, String text, String continuationStr, int width) {
        if (metrics.stringWidth(text) <= width) {
            return text;
        } else {
            for (int i = text.length() - 1; i >= 1; i--) {
                String testText = text.substring(0, i) + continuationStr;
                if (metrics.stringWidth(testText) <= width)
                    return testText;
            }
            return "";
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
