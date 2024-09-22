package com.github.matsud224.waveviz;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class WaveViewModel implements PropertyChangeListener {
    private ArrayList<Waveform> waveforms;
    private ArrayList<Marker> markers;

    private final Marker cursor;

    private Optional<Integer> focusedIndex = Optional.empty();
    private Optional<Integer> selectedIndex = Optional.empty();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public static final String WAVEFORM_PROPERTY = "WAVEFORM_PROPERTY";
    public static final String MARKER_PROPERTY = "MARKER_PROPERTY";

    public WaveViewModel() {
        this.waveforms = new ArrayList<>();
        this.markers = new ArrayList<>();

        this.cursor = new Marker(0, Color.red);
        addMarker(cursor);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public ArrayList<Waveform> getWaveforms() {
        return waveforms;
    }

    public Waveform getWaveform(int index) {
        return waveforms.get(index);
    }

    public int getWaveformCount() {
        return waveforms.size();
    }

    public void addWaveform(Waveform wf) {
        this.waveforms.add(wf);
        wf.addPropertyChangeListener(this);
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public void addWaveform(int index, Waveform wf) {
        this.waveforms.add(index, wf);
        wf.addPropertyChangeListener(this);
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public void removeWaveform(int index) {
        this.waveforms.get(index).removePropertyChangeListener(this);
        this.waveforms.remove(index);
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public void removeAllWaveforms() {
        int count = getWaveformCount();
        for (int i = 0; i < count; i++)
            removeWaveform(i);
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public void addWaveforms(ArrayList<Waveform> waveforms) {
        for (var wf : waveforms) {
            addWaveform(wf);
        }
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public void reorderWaveform(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= getWaveformCount())
            return;
        var target = getWaveform(fromIndex);
        removeWaveform(fromIndex);
        addWaveform(toIndex, target);
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public int getMaxTime() {
        return waveforms.stream().map(wf -> wf.getSignal().getValueChangeStore().getLastTime()).max(Comparator.naturalOrder()).orElse(0);
    }

    public ArrayList<Marker> getMarkers() {
        return markers;
    }

    public void addMarker(Marker m) {
        this.markers.add(m);
        m.addPropertyChangeListener(this);
        this.pcs.firePropertyChange(MARKER_PROPERTY, null, null);
    }

    public void addMarker(int index, Marker m) {
        this.markers.add(index, m);
        m.addPropertyChangeListener(this);
        this.pcs.firePropertyChange(MARKER_PROPERTY, null, null);
    }

    public void removeMarker(int index) {
        getMarker(index).removePropertyChangeListener(this);
        this.markers.remove(index);
        this.pcs.firePropertyChange(MARKER_PROPERTY, null, null);
    }

    public void addMarkers(ArrayList<Marker> markers) {
        for (var m : markers) {
            addMarker(m);
        }
        this.pcs.firePropertyChange(MARKER_PROPERTY, null, null);
    }

    public Marker getMarker(int index) {
        return this.markers.get(index);
    }

    public Marker getCursor() {
        return cursor;
    }

    public void setFocus(int index) {
        if (!this.focusedIndex.equals(Optional.of(index))) {
            if (index >= 0 && index < waveforms.size())
                this.focusedIndex = Optional.of(index);
            else
                this.focusedIndex = Optional.empty();
            this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
        }
    }

    public void invalidateFocus() {
        if (this.focusedIndex.isPresent()) {
            this.focusedIndex = Optional.empty();
            this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
        }
    }

    public void setSelection(int index) {
        if (!this.selectedIndex.equals(Optional.of(index))) {
            if (index >= 0 && index < waveforms.size())
                this.selectedIndex = Optional.of(index);
            else
                this.selectedIndex = Optional.empty();
            this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
        }
    }

    public void invalidateSelection() {
        if (this.selectedIndex.isPresent()) {
            this.selectedIndex = Optional.empty();
            this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
        }
    }

    public Optional<Integer> getFocusedIndex() {
        return focusedIndex;
    }

    public Optional<Integer> getSelectedIndex() {
        return selectedIndex;
    }

    public void moveCursorToFirst() {
        this.cursor.setTime(0);
        this.pcs.firePropertyChange(MARKER_PROPERTY, null, null);
    }

    public void moveCursorToLast() {
        this.cursor.setTime(getMaxTime());
        this.pcs.firePropertyChange(MARKER_PROPERTY, null, null);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.pcs.firePropertyChange(evt);
    }
}
