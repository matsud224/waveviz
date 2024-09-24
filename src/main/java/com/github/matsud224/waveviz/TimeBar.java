package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TimeBar extends JComponent implements ScaleChangeListener, PropertyChangeListener {
    private int increment;
    private int pixelsPerUnitTime = 2;
    private WaveViewModel model;

    public TimeBar(WaveViewModel model, int increment) {
        setModel(model);
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

    private int timeFromXCoordinate(int x) {
        int t;
        if (pixelsPerUnitTime > 0) {
            t = x / pixelsPerUnitTime;
        } else {
            t = WavevizUtilities.safeMultiply(x, -pixelsPerUnitTime).orElse(Integer.MAX_VALUE);
        }
        return t + model.getStartTime();
    }

    private int xCoordinateFromTime(int t) {
        t = t - model.getStartTime();
        if (pixelsPerUnitTime > 0) {
            return WavevizUtilities.safeMultiply(t, pixelsPerUnitTime).orElse(Integer.MAX_VALUE);
        } else {
            return t / (-pixelsPerUnitTime);
        }
    }

    private int pixelsOfTimeSpan(int t) {
        if (pixelsPerUnitTime > 0) {
            return WavevizUtilities.safeMultiply(t, pixelsPerUnitTime).orElse(Integer.MAX_VALUE);
        } else {
            return t / (-pixelsPerUnitTime);
        }
    }

    private String timeToLabel(int time) {
        var ts = model.getTimescale();
        time = ts.getMultiplier() * time;
        var unit = ts.getTimeUnit();
        while (time % 1000 == 0) {
            var nextUnit = unit.getGreaterUnit();
            if (nextUnit.isPresent()) {
                time /= 1000;
                unit = nextUnit.get();
            } else {
                break;
            }
        }
        return time + unit.toString();
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

        if (model.getTimescale() == null)
            return;

        int startTime = timeFromXCoordinate(clipBounds.x);

        int upperY = WavevizSettings.WAVE_Y_PADDING;
        int lowerY = WavevizSettings.TIMEBAR_HEIGHT;

        int timeLabelInterval = 100;
        if (pixelsPerUnitTime > 0) {
            timeLabelInterval /= pixelsPerUnitTime;
        } else {
            timeLabelInterval *= -pixelsPerUnitTime;
        }
        if (timeLabelInterval == 0)
            timeLabelInterval = 1;

        int currentTime = (startTime + (timeLabelInterval - 1)) / timeLabelInterval * timeLabelInterval;
        int currentX = xCoordinateFromTime(currentTime);

        while (currentX < clipBounds.x + clipBounds.width) {
            int rightX = currentX + pixelsOfTimeSpan(timeLabelInterval);

            g2.setColor(WavevizSettings.WAVE_TEXT_COLOR);
            var metrics = g2.getFontMetrics();
            var labelStr = WavevizUtilities.getTextWithinWidth(metrics, timeToLabel(currentTime), "..", rightX - currentX - WavevizSettings.WAVE_LABEL_RIGHT_PADDING * 2);
            g2.drawString(labelStr, currentX + WavevizSettings.WAVE_LABEL_RIGHT_PADDING, WavevizSettings.WAVE_Y_PADDING + WavevizSettings.WAVE_FONT_HEIGHT);

            g2.setColor(WavevizSettings.TIMEBAR_LINE_COLOR);
            g2.drawLine(currentX, (lowerY - upperY) / 2, currentX, lowerY);

            currentX = rightX;
            currentTime += timeLabelInterval;
        }

        g2.setColor(WavevizSettings.TIMEBAR_LINE_COLOR);
        g2.drawLine(clipBounds.x, lowerY, clipBounds.x + clipBounds.width, lowerY);
    }

    @Override
    public void scaleChanged(int pixelsPerUnitTime, int width) {
        setPreferredWidth(width);
        this.pixelsPerUnitTime = pixelsPerUnitTime;
        repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        repaint();
    }

    public void setModel(WaveViewModel model) {
        this.model = model;
    }
}
