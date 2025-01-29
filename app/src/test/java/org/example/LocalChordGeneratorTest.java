package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalChordGeneratorTest {

    private LocalChordGenerator localGenerator;

    @BeforeEach
    void setUp() {
        List<String> tuning = Arrays.asList("G3", "D4", "A4", "E5");
        localGenerator = new LocalChordGenerator(tuning);
    }

    @Test
    void testGenerateChordData() {
        List<Map<String, Object>> chordData = localGenerator.generateChordData("C");
        assertFalse(chordData.isEmpty());
        
        Map<String, Object> firstChord = chordData.get(0);
        assertEquals("C", firstChord.get("Root"));
        assertNotNull(firstChord.get("Quality"));
        assertNotNull(firstChord.get("ChordInfo"));
    }

    @Test
    void testGetScaleData() {
        Map<String, Object> scaleData = localGenerator.getScaleData("C", "major", 2);
        assertEquals("C", scaleData.get("key"));
        assertEquals("major", scaleData.get("scale_type"));
        assertNotNull(scaleData.get("scale"));
        assertNotNull(scaleData.get("chords"));
    }

    @Test
    void testCreateScaleWithSharps() {
        List<String> scale = localGenerator.createScaleWithSharps("C", "major");
        assertFalse(scale.isEmpty());
        assertEquals("C", scale.get(0));
    }

    @Test
    void testGetRomanNumeral() {
        List<String> scale = Arrays.asList("C", "D", "E", "F", "G", "A", "B");
        assertEquals("I", localGenerator.getRomanNumeral(scale, "C", "MAJOR"));
        assertEquals("ii", localGenerator.getRomanNumeral(scale, "D", "MINOR"));
    }
}
