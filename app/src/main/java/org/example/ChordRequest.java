package org.example;

import java.util.List;

public class ChordRequest {
    private String key;
    private String scaleType;
    private int maxVariations;
    private List<String> tuning;
    private int stringCount;
    private int maxFretSpan;
    private boolean allowInversions;
    private boolean allowPartialChords;
    private boolean allowOpenStrings;
    private boolean allowFullChords;

    // Getters
    public String getKey() { return key; }
    public String getScaleType() { return scaleType; }
    public int getMaxVariations() { return maxVariations; }
    public List<String> getTuning() { return tuning; }
    public int getStringCount() { return stringCount; }
    public int getMaxFretSpan() { return maxFretSpan; }
    public boolean getAllowInversions() { return allowInversions; }
    public boolean getAllowPartialChords() { return allowPartialChords; }
    public boolean getAllowOpenStrings() { return allowOpenStrings; }
    public boolean getAllowFullChords() { return allowFullChords; }

    // Setters
    public void setKey(String key) { this.key = key; }
    public void setScaleType(String scaleType) { this.scaleType = scaleType; }
    public void setMaxVariations(int maxVariations) { this.maxVariations = maxVariations; }
    public void setTuning(List<String> tuning) { this.tuning = tuning; }
    public void setStringCount(int stringCount) { this.stringCount = stringCount; }
    public void setMaxFretSpan(int maxFretSpan) { this.maxFretSpan = maxFretSpan; }
    public void setAllowInversions(boolean allowInversions) { this.allowInversions = allowInversions; }
    public void setAllowPartialChords(boolean allowPartialChords) { this.allowPartialChords = allowPartialChords; }
    public void setAllowOpenStrings(boolean allowOpenStrings) { this.allowOpenStrings = allowOpenStrings; }
    public void setAllowFullChords(boolean allowFullChords) { this.allowFullChords = allowFullChords; }
} 