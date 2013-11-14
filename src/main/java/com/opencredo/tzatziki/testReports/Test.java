package com.opencredo.tzatziki.testReports;

public class Test {
    public String id;
    public boolean failing;
    public boolean defectExists;
    public boolean failureExpected;

    public Test (String testId, boolean failing)
    {
        this.id = testId;
        this.failing = failing;
    }

}
