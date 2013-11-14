package com.opencredo.tzatziki.defectTrackers;

import java.util.List;

public interface DefectTracker {

    public boolean defectExistsForTest(String testId);

    //public boolean isFailureExpected(String testId);

    public void createDefect(String defectType, String testIdentifier);
}
