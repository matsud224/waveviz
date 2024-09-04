package com.github.matsud224.waveviz;

import java.util.ArrayList;

public class Signal {
    private final ArrayList<String> path;
    private final String type;
    private final int size;
    private final ValueChangeStore valueChangeStore;

    public Signal(ArrayList<String> path, String type, int size, ValueChangeStore valueChangeStore) {
        this.path = path;
        this.type = type;
        this.size = size;
        this.valueChangeStore = valueChangeStore;
    }

    public ArrayList<String> getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return path.get(path.size() - 1);
    }

    public void print(int depth) {
        for (var i = 0; i < depth; i++)
            System.out.print("-");
        System.out.printf("%s (%s, %d-bit)\n", path, type, size);
    }

    public ValueChangeStore getValueChangeStore() {
        return valueChangeStore;
    }
}
