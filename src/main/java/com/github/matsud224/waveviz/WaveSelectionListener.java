package com.github.matsud224.waveviz;

import java.util.EventListener;
import java.util.Optional;

public interface WaveSelectionListener extends EventListener {
    void waveFocusChanged(Optional<Integer> index);

    void waveSelectionChanged(Optional<Integer> index);
}
