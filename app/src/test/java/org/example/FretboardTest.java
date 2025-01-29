package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

public class FretboardTest {

    @Test
    public void testFretboardCreation() {
        List<Note> tuning = Arrays.asList(
            Note.getInstance("G", 3),
            Note.getInstance("D", 4),
            Note.getInstance("A", 4),
            Note.getInstance("E", 5)
        );
        Fretboard fretboard = new Fretboard(tuning);

        assertEquals(Note.getInstance("G", 3), fretboard.getPitch(0,0));
    }

}
