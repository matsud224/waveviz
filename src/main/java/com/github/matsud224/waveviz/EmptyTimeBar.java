package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class EmptyTimeBar extends JComponent implements PropertyChangeListener {
    private Waveviz wavevizObject;

    public EmptyTimeBar(Waveviz wavevizObject) {
        this.wavevizObject = wavevizObject;
        setPreferredSize(new Dimension(10000, wavevizObject.getTimebarHeight()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g;
        var clipBounds = g.getClipBounds();

        g2.setColor(wavevizObject.getWaveBackgroundColor());
        g2.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        repaint();
    }
}
