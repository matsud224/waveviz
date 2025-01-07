package com.github.matsud224.waveviz;

import java.awt.*;

public class Annotation {
    private final TimeSpan timeSpan;
    private final String text;
    private final Color color;

    public Annotation(TimeSpan timeSpan, String text, Color color) {
        this.timeSpan = timeSpan;
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }

    public TimeSpan getTimeRange() {
        return timeSpan;
    }
}
