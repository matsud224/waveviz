package com.github.matsud224.waveviz;

public interface WaveStatusListener {
    void waveRemoved(int index);

    void waveReordered(int targetIndex, int toIndex);
}
