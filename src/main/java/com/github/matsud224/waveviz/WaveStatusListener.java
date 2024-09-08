package com.github.matsud224.waveviz;

import java.util.EventListener;

public interface WaveStatusListener extends EventListener {
    void waveRemoved(int index);

    void waveReordered(int targetIndex, int toIndex);

    void waveStatusChanged(int index);
}
