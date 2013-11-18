package com.opencredo.tzatziki.testReports;

import java.util.List;

public interface TestReport {
    public List<Test> getFailingTests();

    public List<Test> getAllTests();

    public void appendText(Test test, String text);

    public void ignoreFailure(Test test);
}
