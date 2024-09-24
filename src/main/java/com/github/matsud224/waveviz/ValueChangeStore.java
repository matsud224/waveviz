package com.github.matsud224.waveviz;

import java.util.ArrayList;

public class ValueChangeStore {
    private final ArrayList<Integer> timeArray;
    private final ArrayList<String> valueArray;

    public ValueChangeStore() {
        timeArray = new ArrayList<>();
        valueArray = new ArrayList<>();
    }

    public void addChange(int time, String numStr) {
        String lastValue = !valueArray.isEmpty() ? valueArray.get(valueArray.size() - 1) : "";
        if (!lastValue.equals(numStr)) {
            timeArray.add(time);
            valueArray.add(numStr);
        }
    }

    private int searchStartingTime(int time) {
        int low = 0;
        int high = timeArray.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int midValue = timeArray.get(mid);
            if (midValue > time)
                high = mid - 1;
            else if (midValue < time)
                low = mid + 1;
            else
                return mid;
        }
        return Math.max(high, 0);
    }

    public TimeRange getValue(int time) {
        int startingPos = searchStartingTime(time);
        if (startingPos == timeArray.size() - 1)
            return new TimeRange(timeArray.get(startingPos), timeArray.get(startingPos), valueArray.get(startingPos));
        else
            return new TimeRange(timeArray.get(startingPos), timeArray.get(startingPos + 1) - 1, valueArray.get(startingPos));
    }

    public int getStartTime() {
        return timeArray.get(0);
    }

    public int getEndTime() {
        return timeArray.get(timeArray.size() - 1);
    }
}
