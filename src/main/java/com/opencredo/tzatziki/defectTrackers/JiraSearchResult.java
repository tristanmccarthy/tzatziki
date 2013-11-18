package com.opencredo.tzatziki.defectTrackers;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JiraSearchResult {
    public int issueCount = 0;
    public List<Defect> issues = new ArrayList<>();

    public JiraSearchResult(String resultJson) {
        parseSearchResult(resultJson);
    }

    public void parseSearchResult(String resultJson) {
        try{
            ObjectMapper mapper = new ObjectMapper();

            StringReader resultJsonBuffer = new StringReader(resultJson);
            JsonNode rootNode = mapper.readTree(resultJsonBuffer);
            Iterator<JsonNode> results = rootNode.get("issues").getElements();

            //get headline search details
            issueCount = rootNode.get("total").asInt();

            //get issues
            while(results.hasNext())
            {
                JsonNode result = results.next();
                String issueHeader = result.get("fields").get("summary").asText();
                String issueKey = result.get("key").asText();
                String issueType = result.get("fields").get("issuetype").get("name").asText();
                issues.add(new Defect(issueHeader, issueKey, issueType));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
