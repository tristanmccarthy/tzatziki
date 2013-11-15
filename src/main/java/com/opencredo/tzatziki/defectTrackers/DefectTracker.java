package com.opencredo.tzatziki.defectTrackers;

import com.opencredo.tzatziki.testReports.Test;

import java.util.List;

public interface DefectTracker {

    public boolean defectExists(Test test);

    public boolean failureExpected(Test test);

    public void createDefect(Test test);
}
