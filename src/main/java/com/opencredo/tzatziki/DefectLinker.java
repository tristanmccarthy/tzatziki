package com.opencredo.tzatziki;

import com.opencredo.tzatziki.defectTrackers.DefectTracker;
import com.opencredo.tzatziki.defectTrackers.JiraDefectTracker;
import com.opencredo.tzatziki.testReports.CucumberJsonTestReport;
import com.opencredo.tzatziki.testReports.Test;
import com.opencredo.tzatziki.testReports.TestReport;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.ArrayList;
import java.util.List;

@Mojo(name = "linkdefects")
public class DefectLinker extends AbstractMojo {

    @Parameter( property = "linkdefects.report-type", defaultValue = "cucumber-json")
    private String reportType;

    @Parameter( property = "linkdefects.tracker-type", defaultValue = "jira")
    private String trackerType;

    @Parameter( property = "linkdefects.reportFile")
    private String reportFile;

    @Parameter( property = "linkdefects.new-defect-type", defaultValue = "Bug")
    private String newDefectType;

    public void execute() throws MojoExecutionException
    {
        reportFile = "/home/tris/Documents/Cucumber-JVM-Parallel/target/cucumber-report/autocorrect/autocorrect.json";
        TestReport report;
        if (reportType.equals("cucumber-json")) { report = new CucumberJsonTestReport(reportFile); }
        else { throw new RuntimeException("Report type '" + reportType + "' not defined"); }

        DefectTracker tracker;
        if(trackerType.equals("jira")) { tracker = new JiraDefectTracker("http://localhost:8080", "DP", "Auto Generated Bug", "", ""); }
        else { throw new RuntimeException("Tracker type '" + trackerType + "' not defined"); }

        //store list of new failures (failing tests with no associated defect found) and known failures
        List<Test> newFailures = new ArrayList<Test>();
        List<Test> knownFailures = new ArrayList<Test>();
        for(Test test:report.getFailingTests())
        {
            if(!tracker.defectExistsForTest(test.id)) { newFailures.add(test); }
            else { knownFailures.add(test); }
        }

        //create defects for new failures
        for (Test test : newFailures)
        {
            tracker.createDefect(newDefectType, test.id);
        }

        //update report to ignore known AND expected failures (known might still be new unexpected failures)
        //for (Test test : knownFailures)
        //{
        //    if(!tracker.isFailureExpected(test.id)) { report.ignoreTestFailure(test.id); }
        //}
    }

}
