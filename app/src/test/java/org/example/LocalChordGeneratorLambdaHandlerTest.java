package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class LocalChordGeneratorLambdaHandlerTest {

    private LocalChordGeneratorLambdaHandler handler;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new LocalChordGeneratorLambdaHandler();
        when(context.getLogger()).thenReturn(logger);
    }

    @Test
    void testHandleRequestWithValidInput() {
        ChordRequest request = new ChordRequest();
        request.setKey("C");
        request.setScaleType("MAJOR");
        request.setMaxVariations(3);
        request.setTuning(Arrays.asList("G3", "D4", "A4", "E5"));
        request.setStringCount(4);
        request.setMaxFretSpan(4);
        request.setAllowInversions(true);
        request.setAllowPartialChords(true);
        request.setAllowOpenStrings(true);
        request.setAllowFullChords(true);

        Map<String, Object> response = handler.handleRequest(request, context);

        assertNotNull(response);
        assertEquals("C", response.get("key"));
        assertEquals("MAJOR", response.get("scale_type"));
        assertNotNull(response.get("scale"));
        assertNotNull(response.get("chords"));
    }

    @Test
    void testHandleRequestWithInvalidStringCount() {
        ChordRequest request = new ChordRequest();
        request.setKey("C");
        request.setScaleType("MAJOR");
        request.setTuning(Arrays.asList("G3", "D4", "A4", "E5"));
        request.setStringCount(5); // Incorrect string count

        Map<String, Object> response = handler.handleRequest(request, context);

        assertNotNull(response);
        assertEquals(400, response.get("statusCode"));
        assertTrue(((String) response.get("error")).contains("String count mismatch"));
        assertEquals(5, response.get("provided_string_count"));
        assertEquals(4, response.get("actual_tuning_size"));
    }


    // @Test
    // void testHandleRequestWithFlatKey() {
    //     ChordRequest request = new ChordRequest();
    //     request.setKey("Bb");
    //     request.setScaleType("MAJOR");
    //     request.setMaxVariations(3);
    //     request.setTuning(Arrays.asList("G3", "D4", "A4", "E5"));
    //     request.setStringCount(4);
    //     request.setMaxFretSpan(4);
    //     request.setAllowInversions(true);
    //     request.setAllowPartialChords(true);
    //     request.setAllowOpenStrings(true);
    //     request.setAllowFullChords(true);

    //     Map<String, Object> response = handler.handleRequest(request, context);

    //     assertNotNull(response);
    //     assertNotNull(response.get("scale"), "Scale should be present");
    //     assertNotNull(response.get("chords"), "Chords should be present");
        
    //     // The key might be normalized to A# internally
    //     String responseKey = (String) response.get("key");
    //     assertTrue(responseKey.equals("Bb") || responseKey.equals("A#"), 
    //         "Key should be either Bb or A# (normalized form)");
    // }

    @Test
    void testHandleRequestWithMaxVariations() {
        ChordRequest request = new ChordRequest();
        request.setKey("C");
        request.setScaleType("MAJOR");
        request.setMaxVariations(2);
        request.setTuning(Arrays.asList("G3", "D4", "A4", "E5"));
        request.setStringCount(4);

        Map<String, Object> response = handler.handleRequest(request, context);

        assertNotNull(response);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chords = (List<Map<String, Object>>) response.get("chords");
        for (Map<String, Object> chord : chords) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> variations = (List<Map<String, Object>>) chord.get("variations");
            assertTrue(variations.size() <= 2, "Number of variations should not exceed maxVariations");
        }
    }

    @Test
    void testHandleRequestWithNashvilleNumbers() {
        ChordRequest request = new ChordRequest();
        request.setKey("C");
        request.setScaleType("MAJOR");
        request.setTuning(Arrays.asList("G3", "D4", "A4", "E5"));
        request.setStringCount(4);

        Map<String, Object> response = handler.handleRequest(request, context);

        assertNotNull(response);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chords = (List<Map<String, Object>>) response.get("chords");
        for (Map<String, Object> chord : chords) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> variations = (List<Map<String, Object>>) chord.get("variations");
            for (Map<String, Object> variation : variations) {
                @SuppressWarnings("unchecked")
                Map<String, Object> chordInfo = (Map<String, Object>) variation.get("ChordInfo");
                assertNotNull(chordInfo.get("Nashville Number"), "Nashville Number should be present");
            }
        }
    }
} 