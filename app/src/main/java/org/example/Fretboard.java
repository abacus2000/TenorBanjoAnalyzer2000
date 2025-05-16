package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fretboard {
    private static final List<String> NOTES = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static final Map<String, String> FLAT_TO_SHARP = new HashMap<>();
    private Note[][] fretboard;
    private List<Note> tuning;

    static {
        FLAT_TO_SHARP.put("Bb", "A#");
        FLAT_TO_SHARP.put("Db", "C#");
        FLAT_TO_SHARP.put("Eb", "D#");
        FLAT_TO_SHARP.put("Gb", "F#");
        FLAT_TO_SHARP.put("Ab", "G#");
        FLAT_TO_SHARP.put("Cb", "B");
        FLAT_TO_SHARP.put("Fb", "E");
    }

    public static String convertFlatToSharp(String note) {
        return FLAT_TO_SHARP.getOrDefault(note, note);
    }

    public Fretboard(List<Note> tuning) {
        this.tuning = tuning;
        createFretboard();
    }

    private void createFretboard() {
        int numStrings = tuning.size();
        int numFrets = 12;
        fretboard = new Note[numStrings][numFrets];

        for (int i = 0; i < numStrings; i++) {
            Note stringNote = tuning.get(i);
            String sharpName = convertFlatToSharp(stringNote.getName());
            int startIndex = NOTES.indexOf(sharpName);
            for (int fret = 0; fret < numFrets; fret++) {
                int noteIndex = (startIndex + fret) % 12;
                int octave = stringNote.getOctave() + (startIndex + fret) / 12;
                fretboard[i][fret] = Note.getInstance(NOTES.get(noteIndex), octave);
            }
        }
    }

    public Note getPitch(int string, int fret) {
        return fretboard[string][fret];
    }

    public List<Note> getTuning() {
        return this.tuning;
    }
}
