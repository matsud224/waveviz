package com.github.matsud224.waveviz;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class WavePanel extends JPanel implements ChangeListener {
    private static final int FONT_HEIGHT = 12;
    private static final int WAVE_Y_PADDING = 6;
    private static final int WAVE_LABEL_RIGHT_PADDING = 6;
    private static final Color WAVE_LINE_COLOR = Color.green;
    private static final Color WAVE_HIGH_VALUE_FILL_COLOR = new Color(4, 95, 22);
    private static final Color BACKGROUND_COLOR = Color.black;
    private static final Color TEXT_COLOR = Color.white;
    private static final Color TIME_BAR_COLOR = Color.gray;
    private static final int Y_PADDING = 8;
    private final Font TEXT_FONT;

    private final BoundedRangeModel verticalRangeModel;
    private final BoundedRangeModel horizontalRangeModel;

    private ArrayList<Signal> model;
    private int waveUnitWidth = 40;

    public WavePanel(BoundedRangeModel verticalRangeModel, BoundedRangeModel horizontalRangeModel) {
        this.verticalRangeModel = verticalRangeModel;
        this.verticalRangeModel.addChangeListener(this);
        this.verticalRangeModel.setExtent(1);
        this.verticalRangeModel.setMaximum(0);

        this.horizontalRangeModel = horizontalRangeModel;
        this.horizontalRangeModel.addChangeListener(this);
        this.horizontalRangeModel.setExtent(1);
        this.horizontalRangeModel.setMaximum(1);

        this.model = new ArrayList<>();

        setPreferredSize(new Dimension(4000, 2000));
        TEXT_FONT = new Font("Courier", Font.PLAIN, FONT_HEIGHT);
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, 10000, 10000);
    }

    private int paintTimeBar(Graphics2D g2, int currentY) {
        int columnHeight = FONT_HEIGHT + Y_PADDING * 2;

        int view_start_time = horizontalRangeModel.getValue();

        int currentX = 0;
        int currentTime = view_start_time;

        int upperY = currentY + WAVE_Y_PADDING;
        int lowerY = currentY + columnHeight - WAVE_Y_PADDING;

        int timeLabelInterval = 100;

        currentTime = (currentTime + (timeLabelInterval - 1)) / timeLabelInterval * timeLabelInterval;
        currentX = (currentTime - view_start_time) * waveUnitWidth;

        while (currentX < g2.getClipBounds().width && currentTime < 10000) {
            int rightX = currentX + timeLabelInterval * waveUnitWidth;

            g2.setColor(TEXT_COLOR);
            var metrics = g2.getFontMetrics();
            var labelStr = getTextWithinWidth(metrics, String.format("%d ns", currentTime), "..", rightX - currentX - WAVE_LABEL_RIGHT_PADDING * 2);
            g2.drawString(labelStr, currentX + WAVE_LABEL_RIGHT_PADDING, currentY + WAVE_Y_PADDING + FONT_HEIGHT);

            g2.setColor(TIME_BAR_COLOR);
            g2.drawLine(currentX, (lowerY - upperY) / 2, currentX, lowerY);

            currentX = rightX;
            currentTime += timeLabelInterval;
        }

        g2.setColor(TIME_BAR_COLOR);
        g2.drawLine(g2.getClipBounds().x, lowerY, g2.getClipBounds().x + g2.getClipBounds().width, lowerY);

        return currentY + columnHeight;
    }

    private void paintWaves(Graphics2D g2) {
        int columnHeight = FONT_HEIGHT + Y_PADDING * 2;

        int currentY = 0;
        currentY = paintTimeBar(g2, currentY);

        for (int j = verticalRangeModel.getValue(); j < model.size(); j++) {
            var signal = model.get(j);
            var store = signal.getValueChangeStore();
            int view_start_time = horizontalRangeModel.getValue();

            int currentX = 0;
            int currentTime = view_start_time;

            int upperY = currentY + WAVE_Y_PADDING;
            int lowerY = currentY + columnHeight - WAVE_Y_PADDING;
            String prevValue = null;

            while (currentX < g2.getClipBounds().width && currentTime < store.getLastTime()) {
                TimeRange tr = store.getValue(currentTime);
                int rightX = currentX + waveUnitWidth * (tr.getEndTime() - currentTime + 1);
                if (signal.getSize() == 1) {
                    if (tr.getValue().equals("0")) {
                        g2.setColor(WAVE_LINE_COLOR);
                        g2.drawLine(currentX, lowerY, rightX, lowerY);
                        if (prevValue != null && prevValue.equals("1")) {
                            g2.drawLine(currentX, upperY, currentX, lowerY);
                        }
                    } else if (tr.getValue().equals("1")) {
                        g2.setColor(WAVE_HIGH_VALUE_FILL_COLOR);
                        g2.fillRect(currentX, upperY, rightX - currentX, lowerY - upperY);
                        g2.setColor(WAVE_LINE_COLOR);
                        g2.drawLine(currentX, upperY, rightX, upperY);
                        if (prevValue != null && prevValue.equals("0")) {
                            g2.drawLine(currentX, upperY, currentX, lowerY);
                        }
                    } else {
                        if (tr.getValue().equals("x"))
                            g2.setColor(Color.red);
                        else
                            g2.setColor(Color.yellow);
                        g2.drawLine(currentX, currentY + columnHeight / 2, rightX, currentY + columnHeight / 2);
                    }
                } else {
                    g2.setColor(WAVE_LINE_COLOR);
                    g2.drawLine(currentX, upperY, rightX, upperY);
                    g2.drawLine(rightX, upperY, rightX, lowerY);
                    g2.drawLine(currentX, lowerY, rightX, lowerY);

                    g2.setColor(TEXT_COLOR);
                    var metrics = g2.getFontMetrics();
                    var valStr = getTextWithinWidth(metrics, tr.getValue(), "..", rightX - currentX - WAVE_LABEL_RIGHT_PADDING * 2);
                    g2.drawString(valStr, currentX + WAVE_LABEL_RIGHT_PADDING, currentY + WAVE_Y_PADDING + FONT_HEIGHT);
                }

                currentX = rightX;
                currentTime = tr.getEndTime() + 1;
                prevValue = tr.getValue();
            }
            currentY += columnHeight;
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

        g2.setFont(TEXT_FONT);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintBackground(g2);
        paintWaves(g2);

        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(8));
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        repaint();
    }

    public ArrayList<Signal> getModel() {
        return model;
    }

    public void setModel(ArrayList<Signal> model) {
        this.model = model;
        var maxTime = model.stream().map(s -> s.getValueChangeStore().getLastTime()).max(Comparator.naturalOrder()).orElse(0);
        this.horizontalRangeModel.setExtent((int) Math.log(maxTime));
        this.horizontalRangeModel.setMaximum(maxTime);
        repaint();
    }

    public void zoomIn() {
        if (waveUnitWidth < 10000)
            waveUnitWidth = waveUnitWidth * 2;
        repaint();
    }

    public void zoomOut() {
        waveUnitWidth = waveUnitWidth / 2;
        if (waveUnitWidth <= 0)
            waveUnitWidth = 1;
        repaint();
    }
}
