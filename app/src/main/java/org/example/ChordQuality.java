package org.example;

public enum ChordQuality {
    MAJOR(Interval.UNISON, Interval.MAJOR_THIRD, Interval.PERFECT_FIFTH),
    MINOR(Interval.UNISON, Interval.MINOR_THIRD, Interval.PERFECT_FIFTH),
    DIMINISHED(Interval.UNISON, Interval.MINOR_THIRD, Interval.TRITONE), 
    AUGMENTED(Interval.UNISON, Interval.MAJOR_THIRD, Interval.MINOR_SIXTH);

    private final Interval[] intervals;

    ChordQuality(Interval... intervals) {
        this.intervals = intervals;
    }

    public Interval[] getIntervals() {
        return intervals;
    }
}


