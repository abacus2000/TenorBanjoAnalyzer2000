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



// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;

// import java.util.Arrays;
// import java.util.List;

// class ChordDiagramTest {

//     private Fretboard fretboard;
//     private ChordDiagram majorChord;
//     private ChordDiagram minorChord;

//     @BeforeEach
//     void setUp() {
//         List<Note> tuning = Arrays.asList(
//             Note.getInstance("G", 3),
//             Note.getInstance("D", 4),
//             Note.getInstance("A", 4),
//             Note.getInstance("E", 5)
//         );
//         fretboard = new Fretboard(tuning);
        
//         majorChord = new ChordDiagram(
//             Arrays.asList(2, 2, 2, 0),
//             Arrays.asList(1, 1, 1, 0),
//             Arrays.asList(false, false, false, false),
//             "A",
//             ChordQuality.MAJOR,
//             fretboard
//         );
        
//         minorChord = new ChordDiagram(
//             Arrays.asList(2, 2, 1, 0),
//             Arrays.asList(2, 3, 1, 0),
//             Arrays.asList(false, false, false, false),
//             "A",
//             ChordQuality.MINOR,
//             fretboard
//         );
//     }

//     @Test
//     void testGetNotesWithPitches() {
//         List<Note> majorNotes = majorChord.getNotes();
//         assertEquals(4, majorNotes.size());
//         for (Note note : majorNotes) {
//             System.out.println(note.getName() + note.getOctave());
//         }
//         assertEquals("A", majorNotes.get(0).getName());
//         assertEquals("C#", majorNotes.get(1).getName());
//         assertEquals("E", majorNotes.get(2).getName());
//         assertEquals("A", majorNotes.get(3).getName());
//     }

//     @Test
//     void testFindRootNote() {
//         assertEquals("A", majorChord.getRootNote().getName());
//         assertEquals("A", minorChord.getRootNote().getName());
//     }

//     @Test
//     void testGetPosition() {
//         assertEquals("Fret 2", majorChord.getPosition());
//         assertEquals("Open", new ChordDiagram(
//             Arrays.asList(0, 2, 2, 0),
//             Arrays.asList(0, 1, 2, 0),
//             Arrays.asList(false, false, false, false),
//             "A",
//             ChordQuality.MAJOR,
//             fretboard
//         ).getPosition());
//     }

//     @Test
//     void testGetChordName() {
//         assertEquals("A MAJOR", majorChord.getChordName());
//         assertEquals("A MINOR", minorChord.getChordName());
//     }

//     @Test
//     void testIsPartial() {
//         assertFalse(majorChord.isPartial());
//         assertFalse(minorChord.isPartial());
        
//         ChordDiagram partialChord = new ChordDiagram(
//             Arrays.asList(2, -1, -1, 0),
//             Arrays.asList(1, 0, 0, 0),
//             Arrays.asList(false, true, true, false),
//             "A",
//             ChordQuality.MAJOR,
//             fretboard
//         );
//         assertTrue(partialChord.isPartial());
//     }

//     @Test
//     void testNoteInstanceSingleton() {
//         Note note1 = Note.getInstance("A", 4);
//         Note note2 = Note.getInstance("A", 4);
//         assertSame(note1, note2, "The same Note instance should be returned for the same note and octave");
//     }
// }