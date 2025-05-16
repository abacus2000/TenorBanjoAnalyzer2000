package org.example;

import java.util.List;
import java.util.ArrayList;
// import java.util.Arrays;
import java.util.Collections;

/*
 * frets - list of fret positions for each string for a chord
 * fingers - a 1 if a finger is on a given string, a 0 if not
 * mutes - indicates if a string is muted or should not be played to make the chord
 * root - the root note of the chord as a string
 * quality - (major / minor / etc.)
 * fretboard - the fretboard object 
 * notes - the Notes object 
 * rootNote - the Note object which holds the root position 
 * baseNote - the Note object which holds the base position in the chord
 * inversion - a boolean indicating if the chord is an inversion
 * isPartial - indicates if the chord is only part of a triad 
 * 
 * helpers 
 * getNotesWithPitches() - determines notes of the chord
 * findRootNote() - identifies the root note object
 * findInversion() - determines the inversion of the chord
 */

 public class ChordDiagram {
    private List<Integer> frets;
    private List<Integer> fingers;
    private List<Boolean> mutes;
    private String root;
    private ChordQuality quality;
    private Fretboard fretboard;
    private List<Note> notes;
    private Note rootNote;
    private Note bassNote;
    private int inversion;
    private boolean isPartial;

    public ChordDiagram(List<Integer> frets, List<Integer> fingers, List<Boolean> mutes, 
                        String root, ChordQuality quality, Fretboard fretboard) {
        this.frets = frets;
        this.fingers = fingers;
        this.mutes = mutes;
        this.root = root;
        this.quality = quality;
        this.fretboard = fretboard;
        this.notes = getNotesWithPitches();
        this.rootNote = findRootNote();
        this.bassNote = notes.isEmpty() ? null : notes.get(0);
        this.inversion = findInversion();
        this.isPartial = notes.size() < 3;
    }

    private List<Note> getNotesWithPitches() {
        List<Note> result = new ArrayList<>();
        for (int i = 0; i < frets.size(); i++) {
            if (!mutes.get(i)) {
                result.add(fretboard.getPitch(i, frets.get(i)));
            }
        }
        Collections.sort(result);
        return result;
    }

    private Note findRootNote() {
        for (Note note : notes) {
            if (note.getName().equals(root)) {
                return note;
            }
        }
        return null;
    }

    private int findInversion() {
        if (rootNote == null || notes.isEmpty()) {
            return 0;
        }
        return notes.indexOf(rootNote);
    }

    public String getPosition() {
        List<Integer> playedFrets = new ArrayList<>();
        for (int i = 0; i < frets.size(); i++) {
            if (!mutes.get(i) && frets.get(i) > 0) {
                playedFrets.add(frets.get(i));
            }
        }
        
        if (playedFrets.isEmpty()) {
            return "Open";
        }
        
        int minFret = Collections.min(playedFrets);
        int maxFret = Collections.max(playedFrets);
        
        if (minFret == 0) {
            return "Open";
        } else if (minFret == maxFret && Collections.frequency(fingers, 1) >= 3) {
            return "Barre at fret " + minFret;
        } else {
            return "Fret " + minFret;
        }
    }

    public List<String> getNotesWithStrings() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++) {
            Note tuningNote = fretboard.getTuning().get(i);
            result.add(tuningNote + ":" + notes.get(i));
        }
        return result;
    }

    public String getChordName() {
        String[] inversionNames = {"", " (first inversion)", " (second inversion)"};
        return root + " " + quality.name() + inversionNames[inversion];
    }

    public List<Integer> getUniqueId() {
        return new ArrayList<>(frets);
    }

    public List<Integer> getFrets() { return frets; }
    public List<Integer> getFingers() { return fingers; }
    public List<Boolean> getMutes() { return mutes; }
    public String getRoot() { return root; }
    public ChordQuality getQuality() { return quality; }
    public Fretboard getFretboard() { return fretboard; }
    public List<Note> getNotes() { return notes; }
    public Note getRootNote() { return rootNote; }
    public Note getBassNote() { return bassNote; }
    public int getInversion() { return inversion; }
    public boolean isPartial() { return isPartial; }
}