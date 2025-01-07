package com.github.matsud224.waveviz;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Waveform {
    private final TimeSeries timeSeries;
    private boolean isShowFullPath = false;
    private String displayFormat = "Binary";
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public String getDisplayFormat() {
        return displayFormat;
    }

    public void setDisplayFormat(String displayFormat) {
        this.displayFormat = displayFormat;
        this.pcs.firePropertyChange(WaveViewModel.WAVEFORM_PROPERTY, null, null);
    }

    public Waveform(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    public boolean getIsShowFullPath() {
        return isShowFullPath;
    }

    public void setIsShowFullPath(boolean isShowFullPath) {
        this.isShowFullPath = isShowFullPath;
        this.pcs.firePropertyChange(WaveViewModel.WAVEFORM_PROPERTY, null, null);
    }

    public String getName() {
        var path = timeSeries.getPath();
        return getIsShowFullPath() ? String.join(".", path) : path.get(path.size() - 1);
    }
}
