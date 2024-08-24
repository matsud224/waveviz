package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class WaveInfoPanel extends JPanel implements Scrollable {
    private ArrayList<Signal> model;

    public WaveInfoPanel() {
        this.model = new ArrayList<>();
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(WavevizSettings.WAVE_BACKGROUND_COLOR);
        var r = g2.getClipBounds();
        g2.fillRect(r.x, r.y, r.width, r.height);
    }

    private void paintWaveName(Graphics2D g2) {
        int nowY = WavevizSettings.WAVE_FONT_HEIGHT + WavevizSettings.WAVE_Y_PADDING;
        g2.setColor(Color.white);
        for (Signal signal : model) {
            String w = signal.getName();
            g2.drawString(w, 10, nowY);
            nowY += WavevizSettings.WAVE_FONT_HEIGHT + WavevizSettings.WAVE_Y_PADDING * 2;
        }
    }

    private void paintRowSeparator(Graphics2D g2) {
        int nowY = WavevizSettings.WAVE_FONT_HEIGHT + WavevizSettings.WAVE_Y_PADDING * 2;
        g2.setColor(WavevizSettings.WAVE_ROW_SEPARATOR_COLOR);
        for (int i = 0; i < model.size(); i++) {
            g2.drawLine(0, nowY, 1000, nowY);
            nowY += WavevizSettings.WAVE_FONT_HEIGHT + WavevizSettings.WAVE_Y_PADDING * 2;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(WavevizSettings.WAVE_NORMAL_FONT);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintBackground(g2);
        paintWaveName(g2);
        paintRowSeparator(g2);
    }

    public ArrayList<Signal> getModel() {
        return model;
    }

    public void setModel(ArrayList<Signal> model) {
        this.model = model;
        setPreferredSize(new Dimension(1000, WavevizSettings.WAVE_ROW_HEIGHT * model.size()));
        revalidate();
        repaint();
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
            return WavevizSettings.WAVE_ROW_HEIGHT;
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return 50;
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
