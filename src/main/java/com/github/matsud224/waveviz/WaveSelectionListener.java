package com.github.matsud224.waveviz;

import java.util.Optional;

public interface WaveSelectionListener {
    void waveFocusChanged(Optional<Integer> index);
}
