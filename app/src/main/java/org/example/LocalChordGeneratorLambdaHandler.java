package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import java.util.stream.Collectors;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class LocalChordGeneratorLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Map<String, String> CORS_HEADERS = createCorsHeaders();
    private static final String[] ROMAN_NUMERALS = {"I", "II", "III", "IV", "V", "VI", "VII"};
    private static final List<String> NOTES = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private InstrumentChordGenerator generator;

    private static Map<String, String> createCorsHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,x-api-key,X-Amz-Security-Token");
        headers.put("Access-Control-Max-Age", "600");
        return headers;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        if ("OPTIONS".equals(input.getHttpMethod())) {
            return createResponse(200, "{}");
        }

        try {
            JsonNode requestJson = OBJECT_MAPPER.readTree(input.getBody());
            
            String key = requestJson.get("key").asText();
            String scaleType = requestJson.get("scaleType").asText().toUpperCase();
            int maxVariations = requestJson.get("maxVariations").asInt();
            List<String> tuning = OBJECT_MAPPER.convertValue(requestJson.get("tuning"), 
                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            int stringCount = requestJson.get("stringCount").asInt();
            
            if (stringCount != tuning.size()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "String count mismatch");
                errorResponse.put("provided_string_count", stringCount);
                errorResponse.put("actual_tuning_size", tuning.size());
                errorResponse.put("tuning", tuning);
                
                return createResponse(400, OBJECT_MAPPER.writeValueAsString(errorResponse));
            }

            if (generator == null || !tuning.equals(generator.getTuning())) {
                generator = new InstrumentChordGenerator(tuning);
            }

            Map<String, Object> result = processChordRequest(
                key,
                scaleType,
                maxVariations,
                requestJson.get("allowFullChords").asBoolean(),
                requestJson.get("allowPartialChords").asBoolean(),
                requestJson.get("allowInversions").asBoolean(),
                requestJson.get("allowOpenStrings").asBoolean(),
                requestJson.get("maxFretSpan").asInt()
            );
            
            return createResponse(200, OBJECT_MAPPER.writeValueAsString(result));

        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("error", "Internal server error: " + e.getMessage());
            try {
                return createResponse(500, OBJECT_MAPPER.writeValueAsString(errorBody));
            } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
                return createResponse(500, "{\"error\": \"Internal server error\"}");
            }
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setHeaders(CORS_HEADERS);
        response.setBody(body);
        return response;
    }

    private Map<String, Object> processChordRequest(
            String key,
            String scaleType,
            int maxVariations,
            boolean allowFullChords,
            boolean allowPartialChords,
            boolean allowInversions,
            boolean allowOpenStrings,
            int maxFretSpan) {
        
        List<String> scale = createScale(key, scaleType);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("scale_type", scaleType);
        result.put("scale", scale);
        
        List<Map<String, Object>> chordEntries = generateChordEntries(
            scale, 
            allowFullChords,
            allowPartialChords, 
            allowInversions,
            allowOpenStrings,
            maxFretSpan,
            maxVariations
        );
        
        result.put("chords", chordEntries);
        return result;
    }
    
    private List<Map<String, Object>> generateChordEntries(
            List<String> scale, 
            boolean allowFullChords,
            boolean allowPartialChords, 
            boolean allowInversions,
            boolean allowOpenStrings,
            int maxFretSpan,
            int maxVariations) {
            
        List<Map<String, Object>> formattedChords = new ArrayList<>();
        
        for (String chordRoot : scale) {
            Map<String, Object> chordEntry = new HashMap<>();
            String displayRoot = Note.SHARP_TO_FLAT.getOrDefault(chordRoot, chordRoot);
            chordEntry.put("root", displayRoot);
            
            List<Map<String, Object>> allVariations = new ArrayList<>();
            
            for (ChordQuality quality : ChordQuality.values()) {
                List<ChordDiagram> diagrams = generator.generateChord(
                    chordRoot,
                    quality,
                    allowFullChords,
                    allowPartialChords,
                    allowInversions,
                    allowOpenStrings,
                    maxFretSpan
                );

                if (maxVariations > 0 && diagrams.size() > maxVariations) {
                    diagrams = diagrams.subList(0, maxVariations);
                }

                for (ChordDiagram diagram : diagrams) {
                    Map<String, Object> chordInfo = createChordInfoMap(diagram, quality, scale);
                    allVariations.add(chordInfo);
                }
            }
            
            chordEntry.put("variations", allVariations);
            formattedChords.add(chordEntry);
        }
        
        return formattedChords;
    }
    
    private Map<String, Object> createChordInfoMap(ChordDiagram diagram, ChordQuality quality, List<String> scale) {
        Map<String, Object> chordInfo = new HashMap<>();
        String standardizedRoot = Note.standardizeNote(diagram.getRoot());
        chordInfo.put("Root", standardizedRoot);
        chordInfo.put("Quality", quality.name());
        
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
        
        String nashvilleNumber = getRomanNumeral(scale, standardizedRoot, quality.name());
        details.put("Nashville Number", nashvilleNumber);

        chordInfo.put("ChordInfo", details);
        return chordInfo;
    }

    private List<String> createScale(String key, String scaleType) {
        String sharpKey = Note.standardizeNote(key);
        int rootIndex = NOTES.indexOf(sharpKey);
        
        if (rootIndex == -1) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        
        int[] intervals = {0, 2, 4, 5, 7, 9, 11};
        List<String> scale = new ArrayList<>();
        
        for (int interval : intervals) {
            scale.add(NOTES.get((rootIndex + interval) % 12));
        }
        
        return scale;
    }

    private String getRomanNumeral(List<String> scale, String chordRoot, String quality) {
        int position = scale.indexOf(Note.standardizeNote(chordRoot));
        if (position == -1) {
            return "";
        }
        String romanNumeral = ROMAN_NUMERALS[position];
        return quality.equals("MAJOR") ? romanNumeral : romanNumeral.toLowerCase();
    }

    public Map<String, Object> getScaleData(String key, String scaleType, int maxVariations) {
        List<String> scale = createScale(key, scaleType);
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("scale_type", scaleType);
        result.put("scale", scale);
        
        List<Map<String, Object>> chordEntries = generateChordEntries(
            scale, 
            true,  
            false, 
            true,  
            true, 
            4,    
            maxVariations
        );
        
        result.put("chords", chordEntries);
        return result;
    }
}

