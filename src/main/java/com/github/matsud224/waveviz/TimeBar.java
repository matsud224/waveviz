package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;

public class TimeBar extends JComponent {
    private int height;
    private int increment;

    public TimeBar(int height, int increment) {
        this.height = height;
        this.increment = increment;

        setPreferredSize(new Dimension(10000, height));
    }

    public void setPreferredWidth(int pw) {
        setPreferredSize(new Dimension(pw, height));
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
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        var clipBounds = g.getClipBounds();

        int start = clipBounds.x / increment * increment;
        int end = ((clipBounds.x + clipBounds.width) / increment + 1) * increment;

        for (int x = start; x < end; x += increment) {
            g.drawString(Integer.toString(x), x, 15);
        }
    }
}
