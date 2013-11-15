package com.opencredo.tzatziki.testReports;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CucumberJsonTestReport implements TestReport {
    private List<Test> tests = new ArrayList<Test>();

    public CucumberJsonTestReport(File jsonReport)
    {
        parseReport(jsonReport);
    }

    /**
     * Parse the Cucumber Json report and map to test objects.
     * A test is a step rather than a scenario to prevent duplication
     * of defects
     *
     * @param jsonReport - Test report in Cucumber Json format
     */
    private void parseReport(File jsonReport) {
        List<String> failingTestIds = new ArrayList<String>();

        try{
            ObjectMapper mapper = new ObjectMapper();

            BufferedReader fileReader = new BufferedReader(new FileReader(jsonReport));
            JsonNode rootNode = mapper.readTree(fileReader);

            Iterator<JsonNode> features = rootNode.getElements();

            //loop through features
            while(features.hasNext())
            {
                JsonNode feature = features.next();
                Iterator<JsonNode> scenarios = feature.get("elements").getElements();

                //loop through scenarios in feature
                while(scenarios.hasNext())
                {
                    JsonNode scenario = scenarios.next();
                    Iterator<JsonNode> steps = scenario.get("steps").getElements();

                    //loop through steps in scenario
                    while(steps.hasNext())
                    {
                        //add the test with the hashed step name as identifier and failing status
                        JsonNode step = steps.next();
                        String testId = String.valueOf(step.get("name").asText().hashCode());
                        boolean testFailed = step.get("result").get("status").asText().equals("failed");
                        String content = "Step failure: " + step.get("name").asText();
                        Test test = new Test(testId, testFailed, content);

                        if (!tests.contains(test)) {tests.add(test);}
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Test> getFailingTests() {
        List<Test> failingTests = new ArrayList<Test>();
        for(Test test: tests)
        {
            if (test.failing) { failingTests.add(test); }
        }
        return failingTests;
    }

    public List<Test> getAllTests() {
        return tests;
    }
}
