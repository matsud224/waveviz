package com.github.matsud224.waveviz;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Marker {
    private int time;
    private Color color;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public Marker(int time, Color color) {
        this.time = time;
        this.color = color;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
        this.pcs.firePropertyChange(WaveViewModel.MARKER_PROPERTY, null, null);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.pcs.firePropertyChange(WaveViewModel.MARKER_PROPERTY, null, null);
    }
}
