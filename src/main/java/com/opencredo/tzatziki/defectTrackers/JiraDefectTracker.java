package com.opencredo.tzatziki.defectTrackers;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class JiraDefectTracker implements DefectTracker {
    private String serverAddress;
    private String projectName;
    private String newDefectType;
    private String username;
    private String password;

    public JiraDefectTracker(String serverAddress, String projectName, String newDefectType, String username, String password)
    {
        this.serverAddress = serverAddress;
        this.projectName = projectName;
        this.newDefectType = newDefectType;
        this.username = username;
        this.password = password;
    }

    private CredentialsProvider getCredentialsProvider() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        return credsProvider;
    }

    public boolean defectExistsForTest(String testId) {
        String findDefectUrl = serverAddress + "/rest/api/2/search";
        String queryString = "?jql=text~" + testId;


        try {
            HttpGet findIssues = new HttpGet(findDefectUrl + queryString);
            findIssues.addHeader("Content-Type", "application/json");
            findIssues.addHeader("Authorization", "Basic dHJpc3RhbiBtY2NhcnRoeTp0aGV2ZXJ2ZQ==");

            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(findIssues);

            //verify that the response indicates a success
            System.out.println(EntityUtils.toString(response.getEntity()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createDefect(String defectType, String testIdentifier) {
        String createDefectUrl = serverAddress + "/rest/api/2/issue";
        String createDefectJson =
                "{\n" +
                        "    \"fields\": {\n" +
                        "       \"project\":\n" +
                        "       { \n" +
                        "          \"key\": \"" + projectName + "\"\n" +
                        "       },\n" +
                        "       \"summary\": \"[" + testIdentifier + "] New auto generated defect\",\n" +
                        "       \"description\": \"Defect auto generated by Tzatziki\",\n" +
                        "       \"issuetype\": {\n" +
                        "          \"name\": \"" + newDefectType + "\"\n" +
                        "       }\n" +
                        "   }\n" +
                        "}";

        System.out.println(createDefectJson);
        try {
            HttpPost createDefect = new HttpPost(createDefectUrl);
            createDefect.addHeader("Content-Type", "application/json");
            createDefect.addHeader("Authorization", "Basic dHJpc3RhbiBtY2NhcnRoeTp0aGV2ZXJ2ZQ==");
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
