package com.github.matsud224.waveviz;

import org.jruby.RubyProc;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

public class Waveviz {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Color waveBackgroundColor = Color.black;
    private Color waveFocusedBackgroundColor = Color.gray;
    private Color waveSelectedBackgroundColor = Color.blue;
    private Color waveRowSeparatorColor = Color.white;
    private Color waveTextColor = Color.white;
    private Color waveLineColor = Color.green;
    private Color waveHighValueFillColor = new Color(4, 95, 22);
    private Color timebarLineColor = Color.white;

    private int timebarHeight = 30;
    private int waveLabelRightPadding = 6;

    private int waveYPadding = 8;
    private int waveFontHeight = 12;
    private int waveRowHeight = waveYPadding * 2 + waveFontHeight;
    private int waveMaxPixelsPerUnitTime = 1000;
    private int waveMinWholeWidth = 100;

    private Font waveNormalFont = new Font("Arial", Font.PLAIN, waveFontHeight);
    private Font waveMonospaceFont = new Font("Courier", Font.PLAIN, waveFontHeight - 2);

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

    public Color getWaveFocusedBackgroundColor() {
        return waveFocusedBackgroundColor;
    }

    public void setWaveFocusedBackgroundColor(Color waveFocusedBackgroundColor) {
        this.waveFocusedBackgroundColor = waveFocusedBackgroundColor;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public Color getWaveSelectedBackgroundColor() {
        return waveSelectedBackgroundColor;
    }

    public void setWaveSelectedBackgroundColor(Color waveSelectedBackgroundColor) {
        this.waveSelectedBackgroundColor = waveSelectedBackgroundColor;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public Color getWaveRowSeparatorColor() {
        return waveRowSeparatorColor;
    }

    public void setWaveRowSeparatorColor(Color waveRowSeparatorColor) {
        this.waveRowSeparatorColor = waveRowSeparatorColor;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public Color getWaveTextColor() {
        return waveTextColor;
    }

    public void setWaveTextColor(Color waveTextColor) {
        this.waveTextColor = waveTextColor;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public Color getWaveLineColor() {
        return waveLineColor;
    }

    public void setWaveLineColor(Color waveLineColor) {
        this.waveLineColor = waveLineColor;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public Color getWaveHighValueFillColor() {
        return waveHighValueFillColor;
    }

    public void setWaveHighValueFillColor(Color waveHighValueFillColor) {
        this.waveHighValueFillColor = waveHighValueFillColor;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public Color getTimebarLineColor() {
        return timebarLineColor;
    }

    public void setTimebarLineColor(Color timebarLineColor) {
        this.timebarLineColor = timebarLineColor;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public int getTimebarHeight() {
        return timebarHeight;
    }

    public void setTimebarHeight(int timebarHeight) {
        this.timebarHeight = timebarHeight;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public int getWaveLabelRightPadding() {
        return waveLabelRightPadding;
    }

    public void setWaveLabelRightPadding(int waveLabelRightPadding) {
        this.waveLabelRightPadding = waveLabelRightPadding;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public int getWaveYPadding() {
        return waveYPadding;
    }

    public void setWaveYPadding(int waveYPadding) {
        this.waveYPadding = waveYPadding;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public int getWaveFontHeight() {
        return waveFontHeight;
    }

    public void setWaveFontHeight(int waveFontHeight) {
        this.waveFontHeight = waveFontHeight;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public int getWaveRowHeight() {
        return waveRowHeight;
    }

    public void setWaveRowHeight(int waveRowHeight) {
        this.waveRowHeight = waveRowHeight;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public int getWaveMaxPixelsPerUnitTime() {
        return waveMaxPixelsPerUnitTime;
    }

    public void setWaveMaxPixelsPerUnitTime(int waveMaxPixelsPerUnitTime) {
        this.waveMaxPixelsPerUnitTime = waveMaxPixelsPerUnitTime;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public int getWaveMinWholeWidth() {
        return waveMinWholeWidth;
    }

    public void setWaveMinWholeWidth(int waveMinWholeWidth) {
        this.waveMinWholeWidth = waveMinWholeWidth;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public Font getWaveNormalFont() {
        return waveNormalFont;
    }

    public void setWaveNormalFont(Font waveNormalFont) {
        this.waveNormalFont = waveNormalFont;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }

    public Font getWaveMonospaceFont() {
        return waveMonospaceFont;
    }

    public void setWaveMonospaceFont(Font waveMonospaceFont) {
        this.waveMonospaceFont = waveMonospaceFont;
        this.pcs.firePropertyChange(WAVEFORM_PROPERTY, null, null);
    }
}
