package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstrumentChordGeneratorTest {

    private InstrumentChordGenerator generator;

    @BeforeEach
    void setUp() {
        List<String> tuning = Arrays.asList("G3", "D4", "A4", "E5");
        generator = new InstrumentChordGenerator(tuning);
    }

    @Test
    void testGenerateChord() {
        List<ChordDiagram> diagrams = generator.generateChord("A", ChordQuality.MAJOR, true, false, true, true, 4);
        assertFalse(diagrams.isEmpty());

        ChordDiagram firstDiagram = diagrams.get(0);
        assertEquals("A", firstDiagram.getRoot());
        assertEquals(ChordQuality.MAJOR, firstDiagram.getQuality());
        assertEquals(4, firstDiagram.getFrets().size());
        assertEquals(4, firstDiagram.getFingers().size());
        assertEquals(4, firstDiagram.getMutes().size());
    }

    @Test
    void testGenerateChordWithDifferentQualities() {
        List<ChordDiagram> majorDiagrams = generator.generateChord("C", ChordQuality.MAJOR, true, false, false, true, 4);
        List<ChordDiagram> minorDiagrams = generator.generateChord("C", ChordQuality.MINOR, true, false, false, true, 4);
    

        assertFalse(majorDiagrams.isEmpty(), "Major diagrams should not be empty");
        assertFalse(minorDiagrams.isEmpty(), "Minor diagrams should not be empty");
    

        assertTrue(majorDiagrams.stream().allMatch(d -> d.getQuality() == ChordQuality.MAJOR), 
                   "All major diagrams should have MAJOR quality");
        assertTrue(minorDiagrams.stream().allMatch(d -> d.getQuality() == ChordQuality.MINOR), 
                   "All minor diagrams should have MINOR quality");
    
        assertTrue(majorDiagrams.stream().allMatch(d -> d.getRoot().equals("C")), 
                   "All major diagrams should have root note C");
        assertTrue(minorDiagrams.stream().allMatch(d -> d.getRoot().equals("C")), 
                   "All minor diagrams should have root note C");
    
        boolean diagramsDiffer = majorDiagrams.stream()
            .anyMatch(major -> minorDiagrams.stream()
                .noneMatch(minor -> major.getFrets().equals(minor.getFrets()) && 
                                    major.getNotes().equals(minor.getNotes())));
        
        assertTrue(diagramsDiffer, "Major and minor diagrams should differ in frets or notes");
    }
    

    @Test
    void testGenerateChordWithInversions() {
        List<ChordDiagram> diagrams = generator.generateChord("D", ChordQuality.MAJOR, true, false, true, true, 4);
        boolean hasInversion = diagrams.stream().anyMatch(d -> d.getInversion() > 0);
        assertTrue(hasInversion);
    }

    @Test
    void testGenerateChordWithPartials() {
        List<ChordDiagram> fullChords = generator.generateChord("G", ChordQuality.MAJOR, true, false, false, true, 4);
        List<ChordDiagram> partialChords = generator.generateChord("G", ChordQuality.MAJOR, false, true, false, true, 4);

        assertFalse(fullChords.isEmpty());
        assertFalse(partialChords.isEmpty());
        assertTrue(partialChords.size() > fullChords.size());
    }
}

