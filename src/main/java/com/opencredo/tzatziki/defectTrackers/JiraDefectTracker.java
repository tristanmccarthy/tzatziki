package com.opencredo.tzatziki.defectTrackers;

import com.opencredo.tzatziki.testReports.Test;
import org.apache.http.HttpResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
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

    private JiraSearchResult search(Test test) {
        String findDefectUrl = serverAddress + "/rest/api/2/search";
        String queryString = "?jql=text~" + test.id;
        JiraSearchResult searchResult = null;

        try {
            HttpGet findIssues = new HttpGet(findDefectUrl + queryString);
            findIssues.setHeader("Content-Type", "application/json");
            findIssues.setHeader("Authorization", "Basic " + generateAuthHeader(username, password));

            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(findIssues);

            //parse the json response and return
            searchResult = new JiraSearchResult(EntityUtils.toString(response.getEntity()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    public boolean defectExists(Test test) {
        JiraSearchResult searchResult = search(test);

        //loop through search results and check if the current test id can be matched to an existing defect
        if(searchResult.issueCount > 0) {
            for (Defect issue:searchResult.issues) {
                if(issue.defectHeader.contains(test.id)) { return true;}
            }
        }
        return false;
    }

    public boolean failureExpected(Test test) {
        JiraSearchResult searchResult = search(test);

        //loop through search results and find matching test
        if(searchResult.issueCount > 0) {
            for (Defect issue:searchResult.issues) {
                if(issue.defectHeader.contains(test.id)) {
                    //if defect type does not match 'new defect type', assume the defect has been manually updated and is expected
                    if(!issue.defectType.equals(newDefectType)) { return true; }
                }
            }
        }
        return false;
    }

    public Defect createDefect(Test test) {
        Defect createdDefect = null;
        String createDefectUrl = serverAddress + "/rest/api/2/issue";
        String defectSummary = "[" + test.id + "] New auto generated defect";
        String createDefectJson =
                "{\n" +
                        "    \"fields\": {\n" +
                        "       \"project\":\n" +
                        "       { \n" +
                        "          \"key\": \"" + projectName + "\"\n" +
                        "       },\n" +
                        "       \"summary\": \"" + defectSummary + "\",\n" +
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

            //return details of the defect created
            ObjectMapper mapper = new ObjectMapper();
            StringReader resultJsonReader = new StringReader(EntityUtils.toString(response.getEntity()));
            JsonNode rootNode = mapper.readTree(resultJsonReader);
            String issueKey = rootNode.get("key").asText();

            createdDefect = new Defect(defectSummary, issueKey, newDefectType);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return createdDefect;
    }
}
