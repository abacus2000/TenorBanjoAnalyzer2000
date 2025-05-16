package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class InstrumentChordGenerator {
    private final List<String> tuning;
    private final Fretboard fretboard;

    public InstrumentChordGenerator(List<String> tuning) {
        this.tuning = tuning;
        List<Note> tuningNotes = tuning.stream()
            .map(note -> Note.getInstance(note.substring(0, note.length() - 1), 
                Integer.parseInt(note.substring(note.length() - 1))))
            .collect(Collectors.toList());
        this.fretboard = new Fretboard(tuningNotes);
    }

    public List<String> getTuning() {
        return tuning;
    }

    public List<ChordDiagram> generateChord(String root, ChordQuality quality, boolean allowFullChords,
                                          boolean allowPartialChords, boolean allowInversions,
                                          boolean allowOpenStrings, int maxFretSpan) {
        List<String> chordNotes = getChordNotes(root, quality);
        List<List<int[]>> shapes = buildShape(chordNotes, allowFullChords, allowPartialChords, allowInversions, allowOpenStrings, maxFretSpan);
        Map<List<Integer>, ChordDiagram> uniqueDiagrams = new HashMap<>();

        for (List<int[]> shape : shapes) {
            ChordDiagram diagram = createChordDiagram(shape, root, quality);
            uniqueDiagrams.putIfAbsent(diagram.getFrets(), diagram);
        }

        return new ArrayList<>(uniqueDiagrams.values());
    }

    private List<String> getChordNotes(String root, ChordQuality quality) {
        List<String> notes = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
        
        String sharpRoot = Fretboard.convertFlatToSharp(root);
        int rootIndex = notes.indexOf(sharpRoot);
        
        if (rootIndex == -1) {
            throw new IllegalArgumentException("Invalid root note: " + root);
        }
        
        return Arrays.stream(quality.getIntervals())
                .map(interval -> notes.get((rootIndex + interval.getSemitones()) % 12))
                .collect(Collectors.toList());
    }    

    private List<List<int[]>> buildShape(List<String> chordNotes, boolean allowFullChords, boolean allowPartialChords, 
            boolean allowInversions, boolean allowOpenStrings, int maxFretSpan) {
        List<List<int[]>> shapes = new ArrayList<>();
        if (allowFullChords) {
            shapes.addAll(findFullChord(chordNotes, allowOpenStrings, maxFretSpan));
        }
        if (allowPartialChords) {
            shapes.addAll(findPartialChord(chordNotes, allowOpenStrings, maxFretSpan));
        }
        if (allowInversions) {
            shapes.addAll(findInversions(chordNotes, allowOpenStrings, maxFretSpan));
        }
        return shapes;
    }

    private List<List<int[]>> findFullChord(List<String> chordNotes, boolean allowOpenStrings, int maxFretSpan) {
        List<List<int[]>> shapes = new ArrayList<>();
        int numStrings = fretboard.getTuning().size();
        for (List<Integer> combo : generateCombinations(numStrings, chordNotes.size())) {
            List<int[]> shape = new ArrayList<>();
            for (int i = 0; i < combo.size(); i++) {
                int string = combo.get(i);
                if (allowOpenStrings && fretboard.getPitch(string, 0).getName().equals(chordNotes.get(i))) {
                    shape.add(new int[]{string, 0});
                } else {
                    for (int fret = 0; fret < 12; fret++) {
                        if (fretboard.getPitch(string, fret).getName().equals(chordNotes.get(i))) {
                            shape.add(new int[]{string, fret});
                            break;
                        }
                    }
                }
            }
            if (shape.size() == chordNotes.size() && isValidShape(shape, maxFretSpan)) {
                shapes.add(shape);
            }
        }
        return shapes;
    }

    private List<List<int[]>> findPartialChord(List<String> chordNotes, boolean allowOpenStrings, int maxFretSpan) {
        List<List<int[]>> shapes = new ArrayList<>();
        for (int r = 1; r < chordNotes.size(); r++) {
            for (List<String> subset : generateCombinations(chordNotes, r)) {
                shapes.addAll(findFullChord(subset, allowOpenStrings, maxFretSpan));
            }
        }
        return shapes;
    }

    private List<List<int[]>> findInversions(List<String> chordNotes, boolean allowOpenStrings, int maxFretSpan) {
        List<List<int[]>> shapes = new ArrayList<>();
        List<List<String>> inversions = generatePermutations(chordNotes);
        for (List<String> inversion : inversions) {
            shapes.addAll(findFullChord(inversion, allowOpenStrings, maxFretSpan));
        }
        return shapes;
    }

    private boolean isValidShape(List<int[]> shape, int maxFretSpan) {
        List<Integer> frets = shape.stream().map(s -> s[1]).filter(f -> f > 0).collect(Collectors.toList());
        return frets.isEmpty() || Collections.max(frets) - Collections.min(frets) <= maxFretSpan;
    }

    private ChordDiagram createChordDiagram(List<int[]> shape, String root, ChordQuality quality) {
        List<Integer> frets = new ArrayList<>(Collections.nCopies(fretboard.getTuning().size(), -1));
        List<Integer> fingers = new ArrayList<>(Collections.nCopies(fretboard.getTuning().size(), 0));
        List<Boolean> mutes = new ArrayList<>(Collections.nCopies(fretboard.getTuning().size(), true));

        for (int[] pos : shape) {
            int string = pos[0];
            int fret = pos[1];
            frets.set(string, fret);
            fingers.set(string, fret > 0 ? 1 : 0);
            mutes.set(string, false);
        }

        return new ChordDiagram(frets, fingers, mutes, root, quality, fretboard);
    }

    private List<List<Integer>> generateCombinations(int n, int r) {
        List<List<Integer>> combinations = new ArrayList<>();
        backtrack(combinations, new ArrayList<>(), 0, n, r);
        return combinations;
    }

    private void backtrack(List<List<Integer>> combinations, List<Integer> current, int start, int n, int r) {
        if (current.size() == r) {
            combinations.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < n; i++) {
            current.add(i);
            backtrack(combinations, current, i + 1, n, r);
            current.remove(current.size() - 1);
        }
    }

    private <T> List<List<T>> generateCombinations(List<T> list, int r) {
        List<List<T>> combinations = new ArrayList<>();
        backtrack(combinations, new ArrayList<>(), list, 0, r);
        return combinations;
    }

    private <T> void backtrack(List<List<T>> combinations, List<T> current, List<T> list, int start, int r) {
        if (current.size() == r) {
            combinations.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            backtrack(combinations, current, list, i + 1, r);
            current.remove(current.size() - 1);
        }
    }

    private <T> List<List<T>> generatePermutations(List<T> list) {
        List<List<T>> permutations = new ArrayList<>();
        backtrackPermutations(permutations, new ArrayList<>(), list);
        return permutations;
    }

    private <T> void backtrackPermutations(List<List<T>> permutations, List<T> current, List<T> list) {
        if (current.size() == list.size()) {
            permutations.add(new ArrayList<>(current));
            return;
        }
        for (T item : list) {
            if (!current.contains(item)) {
                current.add(item);
                backtrackPermutations(permutations, current, list);
                current.remove(current.size() - 1);
            }
        }
    }
}
