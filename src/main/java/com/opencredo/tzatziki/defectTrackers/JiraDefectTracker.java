package com.opencredo.tzatziki.defectTrackers;

import com.opencredo.tzatziki.testReports.Test;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Properties;

public class JiraDefectTracker implements DefectTracker {
    private static final String PROPERTY_SERVER_ADDRESS = "jiraServerAddress";
    private static final String PROPERTY_USER_NAME = "jiraUserName";
    private static final String PROPERTY_USER_PASSWORD = "jiraUserPassword";
    private static final String PROPERTY_PROJECT_KEY = "jiraProjectKey";
    private static final String PROPERTY_NEW_DEFECT_TYPE = "jiraNewDefectType";

    private String serverAddress;
    private String projectName;
    private String newDefectType;
    private String username;
    private String password;

    public JiraDefectTracker(Properties jiraSettings)
    {
        this.serverAddress = jiraSettings.getProperty(PROPERTY_SERVER_ADDRESS);
        this.username = jiraSettings.getProperty(PROPERTY_USER_NAME);
        this.password = jiraSettings.getProperty(PROPERTY_USER_PASSWORD);
        this.projectName = jiraSettings.getProperty(PROPERTY_PROJECT_KEY);
        this.newDefectType = jiraSettings.getProperty(PROPERTY_NEW_DEFECT_TYPE);
    }

    private String generateAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        byte[] encoded =  org.apache.commons.codec.binary.Base64.encodeBase64(credentials.getBytes());
        return new String(encoded);
    }

    private String search(Test test) {
        String findDefectUrl = serverAddress + "/rest/api/2/search";
        String queryString = "?jql=text~" + test.id;
        String jsonResponse = "";

        try {
            //HttpGet findIssues = new HttpGet(findDefectUrl + queryString);
            HttpGet findIssues = new HttpGet("http://jira.atlassian.com/rest/api/2/search?jql=text~63456774345");
            findIssues.setHeader("Content-Type", "application/json");
            findIssues.setHeader("Authorization", "Basic " + generateAuthHeader(username, password));

            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(findIssues);

            //parse the json response and verify that a matching defect was found
            jsonResponse = EntityUtils.toString(response.getEntity());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }

    public boolean defectExists(Test test) {
        String searchResponseJson = search(test);

        //parse search result and determine if a matching defect exists
        //TODO
        return false;
    }

    public boolean failureExpected(Test test) {
        String searchResultsJson = search(test);

        //parse the result and get the issue type of the matching defect
        //TODO
        return false;
    }

    public void createDefect(Test test) {
        String createDefectUrl = serverAddress + "/rest/api/2/issue";
        String createDefectJson =
                "{\n" +
                        "    \"fields\": {\n" +
                        "       \"project\":\n" +
                        "       { \n" +
                        "          \"key\": \"" + projectName + "\"\n" +
                        "       },\n" +
                        "       \"summary\": \"[" + test.id + "] New auto generated defect\",\n" +
                        "       \"description\": \"" + test.content +"\",\n" +
                        "       \"issuetype\": {\n" +
                        "          \"name\": \"" + newDefectType + "\"\n" +
                        "       }\n" +
                        "   }\n" +
                        "}";


        try {
            HttpPost createDefect = new HttpPost(createDefectUrl);
            createDefect.setHeader("Content-Type", "application/json");
            createDefect.setHeader("Authorization", "Basic " + generateAuthHeader(username, password));
            createDefect.setEntity(new StringEntity(createDefectJson, "UTF-8"));

            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(createDefect);

            //verify that the response indicates a success
            if(response.getStatusLine().getStatusCode() != 201)
            {
                throw new RuntimeException("Failed to create defect, response: " + EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
