package com.github.matsud224.waveviz;

import java.util.ArrayList;

public interface TimeSeries {
    ArrayList<String> getPath();

    String getType();

    int getWidth();

    void addChange(int time, String numStr);

    TimeRange getValue(int time);

    int getStartTime();

    int getEndTime();
}