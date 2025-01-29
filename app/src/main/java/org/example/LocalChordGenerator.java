package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class LocalChordGenerator {
    private final InstrumentChordGenerator generator;
    private final Map<String, String> flatToSharpMapping;
    private final Map<String, String> sharpToFlatMapping;

    public LocalChordGenerator(List<String> stringTuningAndNumber) {
        this.generator = new InstrumentChordGenerator(stringTuningAndNumber);
        this.flatToSharpMapping = createFlatToSharpMapping();
        this.sharpToFlatMapping = createSharpToFlatMapping();
    }

    private Map<String, String> createFlatToSharpMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("C", "C");
        mapping.put("Db", "C#");
        mapping.put("D", "D");
        mapping.put("Eb", "D#");
        mapping.put("E", "E");
        mapping.put("F", "F");
        mapping.put("Gb", "F#");
        mapping.put("G", "G");
        mapping.put("Ab", "G#");
        mapping.put("A", "A");
        mapping.put("Bb", "A#");
        mapping.put("B", "B");
        mapping.put("Fb", "E");
        mapping.put("Cb", "B");
        mapping.put("Bbb", "A");
        mapping.put("Abb", "G");
        mapping.put("Gbb", "F");
        mapping.put("Fbb", "Eb");
        mapping.put("Ebb", "D");
        mapping.put("Dbb", "C");
        return mapping;
    }

    private Map<String, String> createSharpToFlatMapping() {
        return flatToSharpMapping.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public List<String> createScaleWithSharps(String root, String scaleType) {
        return Arrays.asList("C", "D", "E", "F", "G", "A", "B");
    }

    public String getRomanNumeral(List<String> scale, String chordRoot, String quality) {
        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII"};
        int position = scale.indexOf(chordRoot);
        String romanNumeral = romanNumerals[position];
        return quality.equals("MAJOR") ? romanNumeral : romanNumeral.toLowerCase();
    }

    public List<Map<String, Object>> generateChordData(String root) {
        String sharpRoot = flatToSharpMapping.getOrDefault(root, root);
        List<Map<String, Object>> chordData = new ArrayList<>();

        for (ChordQuality quality : ChordQuality.values()) {
            List<ChordDiagram> diagrams = generator.generateChord(sharpRoot, quality, true, false, true, true, 4);
            for (ChordDiagram diagram : diagrams) {
                Map<String, Object> chordInfo = new HashMap<>();
                chordInfo.put("Root", sharpRoot);
                chordInfo.put("Quality", quality.name());
                Map<String, Object> diagramInfo = new HashMap<>();
                diagramInfo.put("Frets", diagram.getFrets());
                diagramInfo.put("Fingers", diagram.getFingers());
                diagramInfo.put("Mutes", diagram.getMutes());
                diagramInfo.put("Position", diagram.getPosition());
                diagramInfo.put("Inversion", diagram.getInversion());
                diagramInfo.put("IsPartial", diagram.isPartial());
                diagramInfo.put("Notes", diagram.getNotes().stream().map(Note::toString).collect(Collectors.toList()));
                diagramInfo.put("NotesOnStrings", diagram.getNotesWithStrings());
                chordInfo.put("ChordInfo", diagramInfo);
                chordData.add(chordInfo);
            }
        }

        return chordData;
    }

    public Map<String, Object> getScaleData(String key, String scaleType, int maxVariations) {
        List<String> scale = createScaleWithSharps(key, scaleType);
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("scale_type", scaleType);
        result.put("scale", scale);
        List<Map<String, Object>> chords = new ArrayList<>();

        for (String chord : scale) {
            List<Map<String, Object>> chordData = generateChordData(chord);
            List<Map<String, Object>> updatedVariations = new ArrayList<>();

            for (int i = 0; i < Math.min(chordData.size(), maxVariations); i++) {
                Map<String, Object> variation = chordData.get(i);
                String quality = (String) variation.get("Quality");
                String romanNumeral = getRomanNumeral(scale, chord, quality);
                ((Map<String, Object>) variation.get("ChordInfo")).put("Nashville Number", romanNumeral);
                updatedVariations.add(variation);
            }

            Map<String, Object> chordEntry = new HashMap<>();
            chordEntry.put("root", sharpToFlatMapping.getOrDefault(chord, chord));
            chordEntry.put("variations", updatedVariations);
            chords.add(chordEntry);
        }

        result.put("chords", chords);
        return result;
    }
}


