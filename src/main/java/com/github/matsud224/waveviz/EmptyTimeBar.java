package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;

public class EmptyTimeBar extends JComponent {
    public EmptyTimeBar() {
        setPreferredSize(new Dimension(10000, WavevizSettings.TIMEBAR_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g;
        var clipBounds = g.getClipBounds();

        g2.setColor(WavevizSettings.WAVE_BACKGROUND_COLOR);
        g2.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
    }
}
