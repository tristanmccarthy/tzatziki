package com.opencredo.tzatziki.testReports;

public class Test {
    public String id;
    public boolean failing;
    public String content;

    public Test (String id, boolean failing, String content)
    {
        this.id = id;
        this.failing = failing;
        this.content = content;
    }

}
