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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Mojo(name = "linkdefects")
public class DefectLinker extends AbstractMojo {

    //test report parameters
    @Parameter( property = "linkdefects.reportType", defaultValue = "cucumber-json")
    private String reportType;
    @Parameter( property = "linkdefects.reportFile")
    private File reportFile;

    //defect linker properties
    @Parameter( property = "linkdefects.trackerType", defaultValue = "JIRA")
    private String trackerType;

    //JIRA specific properties
    @Parameter( property = "linkdefects.jiraSettings")
    private Properties jiraSettings;

    public void execute() throws MojoExecutionException
    {
        //reportFile = new File("/home/tris/Documents/Cucumber-JVM-Parallel/target/cucumber-report/autocorrect/autocorrect.json");
        //create a report object
        TestReport report;
        if (reportType.toUpperCase().equals("CUCUMBER-JSON")) { report = new CucumberJsonTestReport(reportFile); }
        else { throw new RuntimeException("Report type '" + reportType + "' not defined"); }

        //create a tracker object
        DefectTracker tracker;
        if(trackerType.toUpperCase().equals("JIRA")) { tracker = new JiraDefectTracker(jiraSettings); }
        else { throw new RuntimeException("Tracker type '" + trackerType + "' not defined"); }

        List<Test> newFailures = new ArrayList<>();
        List<Test> knownFailures = new ArrayList<>();
        List<Test> expectedFailures = new ArrayList<>();

        //categorise test results as new failures, known failures or expected failures
        for(Test test:report.getFailingTests())
        {
            if(!tracker.defectExists(test))
            {
                newFailures.add(test);
            }
            else
            {
                knownFailures.add(test);
                //expected failures exist as a subset of known failures
                if(tracker.failureExpected(test)) { expectedFailures.add(test); }
            }
        }

        //TODO create defect for new failures and update report to link to defect

        //TODO update report to link to known defects

        //TODO update report to ignore expected failures
    }

}
