package com.github.matsud224.waveviz;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Waveform {
    private final Signal signal;
    private boolean isShowFullPath = false;
    private DisplayFormat displayFormat = DisplayFormat.HEXADECIMAL;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public enum DisplayFormat {
        BINARY, HEXADECIMAL,
    }

    public DisplayFormat getDisplayFormat() {
        return displayFormat;
    }

    public void setDisplayFormat(DisplayFormat displayFormat) {
        this.displayFormat = displayFormat;
        this.pcs.firePropertyChange(WaveViewModel.WAVEFORM_PROPERTY, null, null);
    }

    public Waveform(Signal signal) {
        this.signal = signal;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public Signal getSignal() {
        return signal;
    }

    public boolean getIsShowFullPath() {
        return isShowFullPath;
    }

    public void setIsShowFullPath(boolean isShowFullPath) {
        this.isShowFullPath = isShowFullPath;
        this.pcs.firePropertyChange(WaveViewModel.WAVEFORM_PROPERTY, null, null);
    }

    public String getName() {
        var path = signal.getPath();
        return getIsShowFullPath() ? String.join(".", path) : path.get(path.size() - 1);
    }
}
