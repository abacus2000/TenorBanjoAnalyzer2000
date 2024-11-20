package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {
    @Test
    public void testNoteCreation() {
        Note note = new Note("A", 4.0);
        assertEquals("A", note.getName());
        assertEquals(4.0, note.getOctave());
    }
}