package org.example;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntervalTest {

    @Test
    public void testIntervalSemitones() {
        assertEquals(0, Interval.UNISON.getSemitones());
        assertEquals(12, Interval.OCTAVE.getSemitones());
    }

    public void testFromSemitons() {
        assertEquals(Interval.MINOR_SECOND, Interval.fromSemitones(1));
    }

}
