package com.github.matsud224.waveviz;

public class TimeSpan {
    private final int startTime;
    private final int endTime;

    public TimeSpan(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }
}
