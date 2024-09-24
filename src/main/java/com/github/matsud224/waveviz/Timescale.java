package com.github.matsud224.waveviz;

import java.util.Optional;

public class Timescale {
    private final int multiplier;
    private final TimeUnit timeUnit;

    public Timescale(int multiplier, TimeUnit timeUnit) {
        if (multiplier != 1 && multiplier != 10 && multiplier != 100) {
            System.out.printf("Warning: invalid timescale multiplier %d was changed to 1.", multiplier);
            this.multiplier = 1;
        } else {
            this.multiplier = multiplier;
        }
        this.timeUnit = timeUnit;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public String toString() {
        return Integer.toString(multiplier) + timeUnit.toString();
    }

    public enum TimeUnit {
        S, MS, US, NS, PS, FS;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public Optional<TimeUnit> getLesserUnit() {
            switch (this) {
                case S:
                    return Optional.of(TimeUnit.MS);
                case MS:
                    return Optional.of(TimeUnit.US);
                case US:
                    return Optional.of(TimeUnit.NS);
                case NS:
                    return Optional.of(TimeUnit.PS);
                case PS:
                    return Optional.of(TimeUnit.FS);
                default:
                    return Optional.empty();
            }
        }

        public Optional<TimeUnit> getGreaterUnit() {
            switch (this) {
                case MS:
                    return Optional.of(TimeUnit.S);
                case US:
                    return Optional.of(TimeUnit.MS);
                case NS:
                    return Optional.of(TimeUnit.US);
                case PS:
                    return Optional.of(TimeUnit.NS);
                case FS:
                    return Optional.of(TimeUnit.PS);
                default:
                    return Optional.empty();
            }
        }
    }
}
