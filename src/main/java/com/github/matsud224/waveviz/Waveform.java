package com.github.matsud224.waveviz;

public class Waveform {
    private final Signal signal;

    public Waveform(Signal signal) {
        this.signal = signal;
    }

    public Signal getSignal() {
        return signal;
    }
}
