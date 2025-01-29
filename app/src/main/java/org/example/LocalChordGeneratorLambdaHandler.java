package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.*;
import java.util.stream.Collectors;

public class LocalChordGeneratorLambdaHandler implements RequestHandler<ChordRequest, Map<String, Object>> {
    private final Map<String, String> flatToSharpMapping;
    private final Map<String, String> sharpToFlatMapping;
    private InstrumentChordGenerator generator;

    public LocalChordGeneratorLambdaHandler() {
        this.flatToSharpMapping = createFlatToSharpMapping();
        this.sharpToFlatMapping = createSharpToFlatMapping();
    }

    @Override
    public Map<String, Object> handleRequest(ChordRequest request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Processing request for key: " + request.getKey());

        // validate tuning and string count
        if (request.getTuning() == null || request.getTuning().isEmpty()) {
            throw new IllegalArgumentException("Tuning cannot be null or empty");
        }

        if (request.getStringCount() != request.getTuning().size()) {
            String errorMessage = String.format(
                "String count mismatch: Provided stringCount is %d but tuning array has %d values: %s",
                request.getStringCount(),
                request.getTuning().size(),
                String.join(", ", request.getTuning())
            );
            logger.log("Error: " + errorMessage);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", errorMessage);
            errorResponse.put("statusCode", 400);
            errorResponse.put("provided_string_count", request.getStringCount());
            errorResponse.put("actual_tuning_size", request.getTuning().size());
            errorResponse.put("tuning", request.getTuning());
            return errorResponse;
        }

        // create generator with requested tuning
        generator = new InstrumentChordGenerator(request.getTuning());

        try {
            return processRequest(request, generator);
        } catch (Exception e) {
            logger.log("Error processing request: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("statusCode", 500);
            errorResponse.put("key", request.getKey());
            errorResponse.put("tuning", request.getTuning());
            return errorResponse;
        }
    }

    private Map<String, Object> processRequest(ChordRequest request, InstrumentChordGenerator generator) {
        Map<String, Object> result = new HashMap<>();
        result.put("key", request.getKey());
        result.put("scale_type", request.getScaleType());

        // create scale
        List<String> scale = createScaleWithSharps(request.getKey(), request.getScaleType());
        result.put("scale", scale);

        // get chord diagrams
        List<ChordDiagram> diagrams = generator.generateChord(
            request.getKey(),
            ChordQuality.valueOf(request.getScaleType()),
            request.getAllowFullChords(),
            request.getAllowPartialChords(),
            request.getAllowInversions(),
            request.getAllowOpenStrings(),
            request.getMaxFretSpan()
        );

        // limit the number of diagrams based on maxVariations
        int maxVariations = request.getMaxVariations();
        if (maxVariations > 0 && diagrams.size() > maxVariations) {
            diagrams = diagrams.subList(0, maxVariations);
        }

        // convert ChordDiagram objects to Maps
        List<Map<String, Object>> chordData = diagrams.stream()
            .map(diagram -> {
                Map<String, Object> chordInfo = new HashMap<>();
                String standardizedRoot = standardizeKey(diagram.getRoot());
                chordInfo.put("Root", standardizedRoot);
                chordInfo.put("Quality", diagram.getQuality());
                
                Map<String, Object> details = new HashMap<>();
                details.put("Frets", diagram.getFrets());
                details.put("Fingers", diagram.getFingers());
                details.put("Mutes", diagram.getMutes());
                details.put("Position", "Fret " + Collections.max(diagram.getFrets()));
                details.put("IsPartial", diagram.isPartial());
                details.put("Inversion", diagram.getInversion());
                details.put("Notes", diagram.getNotes().stream()
                    .map(Note::toString)
                    .collect(Collectors.toList()));
                details.put("NotesOnStrings", diagram.getNotesWithStrings());
                
                // add nashville number
                String nashvilleNumber = getRomanNumeral(scale, standardizedRoot, diagram.getQuality().name());
                details.put("Nashville Number", nashvilleNumber);

                chordInfo.put("ChordInfo", details);
                return chordInfo;
            })
            .collect(Collectors.toList());

        // group variations by root note
        Map<String, List<Map<String, Object>>> chordsByRoot = chordData.stream()
            .collect(Collectors.groupingBy(chord -> (String) chord.get("Root")));

        // format final response
        List<Map<String, Object>> formattedChords = new ArrayList<>();
        for (String chordRoot : scale) {
            Map<String, Object> chordEntry = new HashMap<>();
            chordEntry.put("root", sharpToFlatMapping.getOrDefault(chordRoot, chordRoot));
            chordEntry.put("variations", chordsByRoot.getOrDefault(chordRoot, new ArrayList<>()));
            formattedChords.add(chordEntry);
        }

        result.put("chords", formattedChords);
        return result;
    }

    private Map<String, String> createFlatToSharpMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("Bb", "A#");
        mapping.put("Db", "C#");
        mapping.put("Eb", "D#");
        mapping.put("Gb", "F#");
        mapping.put("Ab", "G#");
        return mapping;
    }

    private Map<String, String> createSharpToFlatMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("A#", "Bb");
        mapping.put("C#", "Db");
        mapping.put("D#", "Eb");
        mapping.put("F#", "Gb");
        mapping.put("G#", "Ab");
        return mapping;
    }

    private String standardizeNoteName(String noteName) {
        // handles double flats
        if (noteName.contains("bb")) {
            switch (noteName) {
                case "Cbb": return "Bb";
                case "Dbb": return "C";
                case "Ebb": return "D";
                case "Fbb": return "Eb";
                case "Gbb": return "F";
                case "Abb": return "G";
                case "Bbb": return "A";
                default: return noteName;
            }
        }
        return noteName;
    }

    public List<String> createScaleWithSharps(String key, String scaleType) {
        // for now, returning a basic major scale - may need to expand this based on scaleType later
        List<String> notes = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
        int startIndex = notes.indexOf(standardizeKey(key));
        List<String> scale = new ArrayList<>();
        
        // major scale intervals generation: ... W W H W W W H (2 2 1 2 2 2 1)
        int[] intervals = {0, 2, 4, 5, 7, 9, 11};
        for (int interval : intervals) {
            scale.add(notes.get((startIndex + interval) % 12));
        }
        return scale;
    }

    private String getRomanNumeral(List<String> scale, String chordRoot, String quality) {
        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII"};
        int position = scale.indexOf(standardizeKey(chordRoot));
        if (position == -1) {
            return ""; // return empty string if chord not found in scale
        }
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

    private String standardizeKey(String key) {
        // first... check if it's a flat key that needs to be converted
        String standardKey = flatToSharpMapping.getOrDefault(key, key);
        if (!Arrays.asList("A", "B", "C", "D", "E", "F", "G",
                          "A#", "C#", "D#", "F#", "G#").contains(standardKey)) {
            throw new IllegalArgumentException(
                String.format("Invalid key: %s. Key must be one of: A, B, C, D, E, F, G " +
                            "or their sharp/flat variants", key)
            );
        }
        return standardKey;
    }
}

