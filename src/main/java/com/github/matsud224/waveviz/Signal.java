package com.github.matsud224.waveviz;

import java.util.ArrayList;

public class Signal implements TimeSeries {
    private final ArrayList<String> path;
    private final String type;
    private final int width;
    private final ValueChangeStore valueChangeStore;

    public Signal(ArrayList<String> path, String type, int width, ValueChangeStore valueChangeStore) {
        this.path = path;
        this.type = type;
        this.width = width;
        this.valueChangeStore = valueChangeStore;
    }

    @Override
    public ArrayList<String> getPath() {
        return path;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void addChange(int time, String numStr) {
        valueChangeStore.addChange(time, numStr);
    }

    @Override
    public TimeSpan getValue(int time) {
        return valueChangeStore.getValue(time);
    }

    @Override
    public int getStartTime() {
        return valueChangeStore.getStartTime();
    }

    @Override
    public int getEndTime() {
        return valueChangeStore.getEndTime();
    }

    @Override
    public String toString() {
        return path.get(path.size() - 1);
    }

    public void print(int depth) {
        for (var i = 0; i < depth; i++)
            System.out.print("-");
        System.out.printf("%s (%s, %d-bit)\n", path, type, width);
    }
}
