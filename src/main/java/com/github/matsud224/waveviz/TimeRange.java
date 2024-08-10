package com.github.matsud224.waveviz;

public class TimeRange {
    private final int startTime;
    private final int endTime;
    private final String value;

    public TimeRange(int startTime, int endTime, String value) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.value = value;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public String getValue() {
        return value;
    }
}
