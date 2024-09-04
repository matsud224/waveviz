package com.github.matsud224.waveviz;

public class Waveform {
    private final Signal signal;
    private boolean isShowFullPath = false;

    public Waveform(Signal signal) {
        this.signal = signal;
    }

    public Signal getSignal() {
        return signal;
    }

    public boolean getIsShowFullPath() {
        return isShowFullPath;
    }

    public void setIsShowFullPath(boolean isShowFullPath) {
        this.isShowFullPath = isShowFullPath;
    }

    public String getName() {
        var path = signal.getPath();
        return getIsShowFullPath() ? String.join(".", path) : path.get(path.size() - 1);
    }
}
