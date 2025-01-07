package com.github.matsud224.waveviz;

public class ValueWithSpan<T> {
    private final T value;
    private final TimeSpan timeSpan;

    public ValueWithSpan(T value, TimeSpan timeSpan) {
        this.value = value;
        this.timeSpan = timeSpan;
    }

    public T getValue() {
        return value;
    }

    public TimeSpan getTimeSpan() {
        return timeSpan;
    }
}
