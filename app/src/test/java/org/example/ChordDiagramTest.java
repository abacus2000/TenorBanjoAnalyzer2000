package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChordDiagramTest {

    private ChordDiagram chordDiagram;
    private Fretboard fretboard;

    @BeforeEach
    void setUp() {
        List<Note> tuning = Arrays.asList(
            Note.getInstance("G", 3),
            Note.getInstance("D", 4),
            Note.getInstance("A", 4),
            Note.getInstance("E", 5)
        );
        fretboard = new Fretboard(tuning);
        chordDiagram = new ChordDiagram(
            Arrays.asList(2, 2, 2, 0),
            Arrays.asList(1, 1, 1, 0),
            Arrays.asList(false, false, false, false),
            "A",
            ChordQuality.MAJOR,
            fretboard
        );
    }

    @Test
    void testGetNotes() {
        List<Note> expectedNotes = Arrays.asList(
            Note.getInstance("A", 3),
            Note.getInstance("E", 4),
            Note.getInstance("B", 4),
            Note.getInstance("E", 5)
        );
        assertEquals(expectedNotes, chordDiagram.getNotes());
    }

    @Test
    void testGetPosition() {
        assertEquals("Barre at fret 2", chordDiagram.getPosition());
    }

    @Test
    void testGetFrets() {
        List<Integer> expectedFrets = Arrays.asList(2, 2, 2, 0);
        assertEquals(expectedFrets, chordDiagram.getFrets());
    }

    @Test
    void testGetFingers() {
        List<Integer> expectedFingers = Arrays.asList(1, 1, 1, 0);
        assertEquals(expectedFingers, chordDiagram.getFingers());
    }
}
