package org.example;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class ChordQualityTest {

    @Test
    public void testChordQualityIntervals() {
        assertArrayEquals(new Interval []{Interval.UNISON, Interval.MAJOR_THIRD, Interval.PERFECT_FIFTH}, ChordQuality.MAJOR.getIntervals());
    }
}
