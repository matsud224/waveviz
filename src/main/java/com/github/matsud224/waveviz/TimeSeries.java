package com.github.matsud224.waveviz;

import java.util.ArrayList;

public interface TimeSeries {
    ArrayList<String> getPath();

    String getType();

    ArrayList<TimeSpan> getValue(int time);

    int getStartTime();

    int getEndTime();
}