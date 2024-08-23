package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Comparator;

public class WavePanel extends JPanel implements ComponentListener, Scrollable {
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

    private ArrayList<Signal> model;
    private int waveUnitWidth = 40;

    public WavePanel() {
        this.model = new ArrayList<>();

        TEXT_FONT = new Font("Courier", Font.PLAIN, FONT_HEIGHT);

        addComponentListener(this);
        setPreferredSize(new Dimension(50 * 200, 50 * 100));
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, 10000, 10000);
    }

    private int paintTimeBar(Graphics2D g2, int currentY) {
        int columnHeight = FONT_HEIGHT + Y_PADDING * 2;

        int view_start_time = g2.getClipBounds().x / waveUnitWidth;

        int upperY = currentY + WAVE_Y_PADDING;
        int lowerY = currentY + columnHeight - WAVE_Y_PADDING;

        int timeLabelInterval = 100;

        int currentTime = (view_start_time + (timeLabelInterval - 1)) / timeLabelInterval * timeLabelInterval;
        int currentX = (currentTime - view_start_time) * waveUnitWidth;

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

        for (int j = g2.getClipBounds().x / waveUnitWidth; j < model.size(); j++) {
            var signal = model.get(j);
            var store = signal.getValueChangeStore();
            int view_start_time = g2.getClipBounds().x;

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

        // --- test draw begin ---
        var r = g2.getClipBounds();
        int boxSize = 50;
        int startBoxX = (int) r.getX() / boxSize;
        int startBoxY = (int) r.getY() / boxSize;
        int numBoxX = (int) r.getWidth() / boxSize + 2;
        int numBoxY = (int) r.getHeight() / boxSize + 2;

        int count = 0;
        for (int y = startBoxY; y < startBoxY + numBoxY; y++) {
            for (int x = startBoxX; x < startBoxX + numBoxX; x++) {
                g2.setColor(new Color(Math.min(y * 4, 255), Math.min(x * 4, 255), 10));
                g2.fillRect(x * boxSize, y * boxSize, boxSize, boxSize);
                g2.setColor(Color.black);
                g2.drawRect(x * boxSize, y * boxSize, boxSize, boxSize);
                count++;
            }
        }
        System.out.printf("start=(%d, %d), num=(%d,%d), painted=%d\n", startBoxX, startBoxY, numBoxX, numBoxY, count);
        // --- test draw end ---

        //paintBackground(g2);
        //paintWaves(g2);
    }

    private void update() {
        var maxTime = model.stream().map(s -> s.getValueChangeStore().getLastTime()).max(Comparator.naturalOrder()).orElse(0);
        int columnHeight = FONT_HEIGHT + Y_PADDING * 2;
        setPreferredSize(new Dimension(maxTime * waveUnitWidth, model.size() * columnHeight));
        repaint();
    }

    public ArrayList<Signal> getModel() {
        return model;
    }

    public void setModel(ArrayList<Signal> model) {
        this.model = model;
        update();
        repaint();
    }

    public void zoomIn() {
        if (waveUnitWidth < 10000)
            waveUnitWidth = waveUnitWidth * 2;
        update();
        repaint();
    }

    public void zoomOut() {
        waveUnitWidth = waveUnitWidth / 2;
        if (waveUnitWidth <= 0)
            waveUnitWidth = 1;
        update();
        repaint();
    }

    @Override
    public void componentResized(ComponentEvent componentEvent) {
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {

    }

    @Override
    public void componentShown(ComponentEvent componentEvent) {

    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {

    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return null;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return 25;
        } else {
            return 50;
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return 50;
        } else {
            return 100;
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
