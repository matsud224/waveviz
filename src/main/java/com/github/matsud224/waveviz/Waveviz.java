package com.github.matsud224.waveviz;

import org.jruby.RubyProc;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

public class Waveviz {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Color waveBackgroundColor = Color.black;
    private HashMap<String, RubyProc> formatters = new HashMap<>();

    public static final String WAVEFORM_PROPERTY = "WAVEFORM_PROPERTY";

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public Color getWaveBackgroundColor() {
        return waveBackgroundColor;
    }

    public void setWaveBackgroundColor(Color waveBackgroundColor) {
        this.waveBackgroundColor = waveBackgroundColor;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public void registerFormatter(String name, RubyProc proc) {
        formatters.put(name, proc);
        System.out.printf("Formatter \"%s\" is registered.\n", name);
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public HashMap<String, RubyProc> getFormatters() {
        return formatters;
    }
}
