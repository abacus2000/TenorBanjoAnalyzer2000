package org.example;

import java.util.Arrays;
import java.util.List;

public class Fretboard {
    private static final List<String> NOTES = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private Note[][] fretboard;
    private List<Note> tuning;

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
            int startIndex = NOTES.indexOf(stringNote.getName());
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
