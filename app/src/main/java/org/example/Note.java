package org.example;

public class Note {
    private String name;
    private double octave;

    public Note(String name, double octave) {
        this.name = name;
        this.octave = octave;
    }

    public String getName() {
        return this.name;
    }

    public double getOctave() {
        return this.octave;
    }

    @Override
    public String toString() {
        return "Note{name='" + name + "', octave" + octave + "}";
    }

}
