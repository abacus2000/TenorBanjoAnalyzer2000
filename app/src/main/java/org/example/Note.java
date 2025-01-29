package org.example;

import java.util.HashMap;
import java.util.Map;

public class Note implements Comparable<Note> {
    private String name;
    private int octave;
    private static final String[] NOTES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    // private static Note instance map;
    private static final Map<String, Note> notesMap = new HashMap<>();

    private Note(String name, int octave) {
        this.name = name; 
        this.octave = octave;
    }

    public static Note getInstance(String name, int octave) {
        String key = name + octave;
        if (!notesMap.containsKey(key)) {
            notesMap.put(key, new Note(name, octave));
        }
        return notesMap.get(key);
    }

    public String getName() {
        return this.name;
    }

    public int getOctave() {
        return this.octave;
    }

    @Override
    public String toString() {
        return name + octave;
    }

    @Override
    public int compareTo(Note other) {
        if (this.octave != other.octave ) {
            return Integer.compare(this.octave, other.octave);
        }
        return Integer.compare(getNoteIndex(this.name), getNoteIndex(other.name));
    }

    private int getNoteIndex(String noteName) {
        for (int i = 0; i< NOTES.length; i++) {
            if (NOTES[i].equals(noteName)) {
                return i;
            }
        }

        return -1; // return -1 if note name does not exist in the list of notes (will occure if a note is flat)
    }

}
