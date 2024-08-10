package com.github.matsud224.waveviz;

public class Signal {
    private final String name;
    private final String type;
    private final int size;
    private final ValueChangeStore valueChangeStore;

    public Signal(String name, String type, int size, ValueChangeStore valueChangeStore) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.valueChangeStore = valueChangeStore;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return name;
    }

    public void print(int depth) {
        for (var i = 0; i < depth; i++)
            System.out.print("-");
        System.out.printf("%s (%s, %d-bit)\n", name, type, size);
    }

    public ValueChangeStore getValueChangeStore() {
        return valueChangeStore;
    }
}
