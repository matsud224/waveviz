package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;

public class TimeBar extends JComponent {
    private int increment;
    private int waveUnitWidth = 2;

    public TimeBar(int increment) {
        this.increment = increment;

        setPreferredSize(new Dimension(10000, WavevizSettings.TIMEBAR_HEIGHT));
    }

    public void setPreferredWidth(int pw) {
        setPreferredSize(new Dimension(pw, WavevizSettings.TIMEBAR_HEIGHT));
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g;
        g2.setFont(WavevizSettings.WAVE_NORMAL_FONT);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        var clipBounds = g.getClipBounds();

        g2.setColor(WavevizSettings.WAVE_BACKGROUND_COLOR);
        g2.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

        int startTime = clipBounds.x / waveUnitWidth;

        int upperY = WavevizSettings.WAVE_Y_PADDING;
        int lowerY = WavevizSettings.TIMEBAR_HEIGHT;

        int timeLabelInterval = 100;

        int currentTime = (startTime + (timeLabelInterval - 1)) / timeLabelInterval * timeLabelInterval;
        int currentX = (currentTime - startTime) * waveUnitWidth;

        while (currentX < clipBounds.width && currentTime < 10000) {
            int rightX = currentX + timeLabelInterval * waveUnitWidth;

            g2.setColor(WavevizSettings.WAVE_TEXT_COLOR);
            var metrics = g2.getFontMetrics();
            var labelStr = WavevizUtilities.getTextWithinWidth(metrics, String.format("%d ns", currentTime), "..", rightX - currentX - WavevizSettings.WAVE_LABEL_RIGHT_PADDING * 2);
            g2.drawString(labelStr, currentX + WavevizSettings.WAVE_LABEL_RIGHT_PADDING, WavevizSettings.WAVE_Y_PADDING + WavevizSettings.WAVE_FONT_HEIGHT);

            g2.setColor(WavevizSettings.TIMEBAR_LINE_COLOR);
            g2.drawLine(currentX, (lowerY - upperY) / 2, currentX, lowerY);

            currentX = rightX;
            currentTime += timeLabelInterval;
        }

        g2.setColor(WavevizSettings.TIMEBAR_LINE_COLOR);
        g2.drawLine(clipBounds.x, lowerY, clipBounds.x + clipBounds.width, lowerY);
    }
}
