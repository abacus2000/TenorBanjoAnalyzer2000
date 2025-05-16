package org.example;

import java.util.HashMap;
import java.util.Map;

public class Note implements Comparable<Note> {
    private String name;
    private int octave;
    private static final String[] NOTES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final Map<String, String> FLAT_TO_SHARP = new HashMap<>();
    public static final Map<String, String> SHARP_TO_FLAT = new HashMap<>();
    public static final Map<String, String> DOUBLE_FLAT_MAP = new HashMap<>();
    private static final Map<String, Note> notesMap = new HashMap<>();

    static {

        FLAT_TO_SHARP.put("Bb", "A#");
        FLAT_TO_SHARP.put("Db", "C#");
        FLAT_TO_SHARP.put("Eb", "D#");
        FLAT_TO_SHARP.put("Gb", "F#");
        FLAT_TO_SHARP.put("Ab", "G#");
        

        for (Map.Entry<String, String> entry : FLAT_TO_SHARP.entrySet()) {
            SHARP_TO_FLAT.put(entry.getValue(), entry.getKey());
        }
        

        DOUBLE_FLAT_MAP.put("Cbb", "A#");
        DOUBLE_FLAT_MAP.put("Dbb", "C");
        DOUBLE_FLAT_MAP.put("Ebb", "D");
        DOUBLE_FLAT_MAP.put("Fbb", "D#");
        DOUBLE_FLAT_MAP.put("Gbb", "F");
        DOUBLE_FLAT_MAP.put("Abb", "G");
        DOUBLE_FLAT_MAP.put("Bbb", "A");
    }

    private Note(String name, int octave) {
        this.name = name;
        this.octave = octave;
    }

    public static Note getInstance(String name, int octave) {
        String sharpName = standardizeNote(name);
        String key = sharpName + octave;
        if (!notesMap.containsKey(key)) {
            notesMap.put(key, new Note(sharpName, octave));
        }
        return notesMap.get(key);
    }

    public static String standardizeNote(String noteName) {

        if (noteName.contains("bb")) {
            return DOUBLE_FLAT_MAP.getOrDefault(noteName, noteName);
        }
        return FLAT_TO_SHARP.getOrDefault(noteName, noteName);
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
        if (this.octave != other.octave) {
            return Integer.compare(this.octave, other.octave);
        }
        return Integer.compare(getNoteIndex(this.name), getNoteIndex(other.name));
    }

    private int getNoteIndex(String noteName) {
        String sharpName = standardizeNote(noteName);
        for (int i = 0; i < NOTES.length; i++) {
            if (NOTES[i].equals(sharpName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid note name: " + noteName);
    }
}
