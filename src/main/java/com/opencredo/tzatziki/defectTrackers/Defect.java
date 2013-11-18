package com.opencredo.tzatziki.defectTrackers;

public class Defect {
    public String defectHeader;
    public String defectId;
    public String defectType;

    public Defect(String defectHeader, String defectId, String defectType) {
        this.defectHeader = defectHeader;
        this.defectId = defectId;
        this.defectType = defectType;
    }

}
