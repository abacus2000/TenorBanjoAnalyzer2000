package org.example;

import java.util.Arrays;
import java.util.List;


public class ChordDiagramDebugger {
    private Fretboard fretboard;
    private ChordDiagram chord;

    public ChordDiagramDebugger(List<Note> tuning, List<Integer> frets, List<Integer> fingers, 
                                List<Boolean> mutes, String root, ChordQuality quality) {
        this.fretboard = new Fretboard(tuning);
        this.chord = new ChordDiagram(frets, fingers, mutes, root, quality, fretboard);
    }

    public void debugGetNotesWithPitches() {
        System.out.println("Debugging getNotesWithPitches:");
        List<Note> notes = chord.getNotes();
        for (int i = 0; i < notes.size(); i++) {
            System.out.println("Note " + i + ": " + notes.get(i).getName() + notes.get(i).getOctave());
        }
    }

    public void debugGetPosition() {
        System.out.println("Debugging getPosition:");
        System.out.println("Position: " + chord.getPosition());
        System.out.println("Frets: " + chord.getFrets());
        System.out.println("Fingers: " + chord.getFingers());
    }

    public void debugAll() {
        debugGetNotesWithPitches();
        debugGetPosition();
    }

        public static void main(String[] args) {
        List<Note> tuning = Arrays.asList(
            Note.getInstance("G", 3),
            Note.getInstance("D", 4),
            Note.getInstance("A", 4),
            Note.getInstance("E", 5)
        );
        
        ChordDiagramDebugger debugger = new ChordDiagramDebugger(
            tuning,
            Arrays.asList(2, 2, 2, 0),
            Arrays.asList(1, 1, 1, 0),
            Arrays.asList(false, false, false, false),
            "A",
            ChordQuality.MAJOR
        );
        
        debugger.debugAll();
    }
}

