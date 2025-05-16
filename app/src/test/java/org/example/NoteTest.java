package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {
    @Test
    public void testSingletonSame() {
        Note instance1 = Note.getInstance("A", 4);
        Note instance2 = Note.getInstance("A", 4);

        assertSame(instance1, instance2, "Singleton should return the same instance");
    }

    @Test
    public void testSingletonDifferentNote() {
        Note note1 = Note.getInstance("A", 4);
        Note note2 = Note.getInstance("B", 4);

        assertNotSame(note1, note2, "Notes with different inputs should map to different instances of the Note class");
    }

    @Test
    public void testSingletonDifferentOctaves() {
        Note note1 = Note.getInstance("A", 4);
        Note note2 = Note.getInstance("A", 5);
        assertNotSame(note1, note2, "Note with different octaves should not be the same instance");
    }

    @Test
    public void testNoteCreation() {
        Note note3 = Note.getInstance("A", 4);
        assertEquals("A", note3.getName());
        assertEquals(4, note3.getOctave());
    }

    // @Test
    // public void testToString() {
    //     Note note = new Note("C#", 3);
    //     assertEquals("C#3", note.toString());
    // }
}