package com.example.jirascraper;

import com.example.jirascraper.model.JiraIssue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class JiraIssueFetcher {
    private final JiraClient client;
    private final String project;
    private final int PAGE_SIZE;

    public JiraIssueFetcher(JiraClient client,String project,int PAGE_SIZE) {
        this.client = client;
        this.project = project;
        this.PAGE_SIZE = PAGE_SIZE;
    }

    /**
     * Fetch a page of issues from JIRA
     * @param startAt offset for pagination
     * @return JSONObject response with "issues" array and metadata
     */
    public JSONObject fetchIssuesPage( int startAt) throws IOException {
        String jql = String.format("project=%s",project);
        String encodedJql = URLEncoder.encode(jql, StandardCharsets.UTF_8);
        String fields = "summary,description,comment,priority,status,assignee,reporter,labels,created,updated,issuetype";

        String endpoint = String.format(
                "/search?jql=%s&startAt=%d&maxResults=%d&fields=%s",
                encodedJql, startAt, PAGE_SIZE, URLEncoder.encode(fields, StandardCharsets.UTF_8)
        );

        return client.get(endpoint);
    }

    /**
     * Parse individual issue from JSON
     * @param issue JSONObject representing a single JIRA issue
     * @return JiraIssue object
     */
    public JiraIssue parseIssue(JSONObject issue) {
        JSONObject fields = issue.optJSONObject("fields");

        JiraIssue jiraIssue = new JiraIssue();
        jiraIssue.key = issue.optString("key");
        jiraIssue.summary = safe(fields, "summary");
        jiraIssue.description = safe(fields, "description");
        jiraIssue.issueType = nestedSafe(fields, "issuetype", "name");
        jiraIssue.status = nestedSafe(fields, "status", "name");
        jiraIssue.priority = nestedSafe(fields, "priority", "name");
        jiraIssue.assignee = nestedSafe(fields, "assignee", "displayName");
        jiraIssue.reporter = nestedSafe(fields, "reporter", "displayName");
        jiraIssue.labels = joinArray(fields.optJSONArray("labels"));
        jiraIssue.created = safe(fields, "created");
        jiraIssue.updated = safe(fields, "updated");

        // Comments
        JSONObject commentObj = fields.optJSONObject("comment");
        if (commentObj != null) {
            jiraIssue.commentCount = commentObj.optInt("total", 0);
            JSONArray comments = commentObj.optJSONArray("comments");
            if (comments != null) {
                StringBuilder commentsText = new StringBuilder();
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject c = comments.getJSONObject(i);
                    String author = c.getJSONObject("author").optString("displayName", "Unknown");
                    String created = c.optString("created", "");
                    String body = c.optString("body", "").replaceAll("\\s+", " ").trim();
                    commentsText.append("[").append(author).append(" @ ").append(created).append("]: ")
                            .append(body).append("\n");
                }
                jiraIssue.commentsText = commentsText.toString().trim();
            }
        }

        return jiraIssue;
    }

    // ----------------- Utility Methods -----------------

    private static String safe(JSONObject obj, String key) {
        if (obj == null) return "";
        return obj.optString(key, "");
    }

    private static String nestedSafe(JSONObject obj, String parent, String child) {
        if (obj == null) return "";
        JSONObject p = obj.optJSONObject(parent);
        if (p != null) return p.optString(child, "");
        return "";
    }

    private static String joinArray(JSONArray arr) {
        if (arr == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (i > 0) sb.append("|");
            sb.append(arr.optString(i));
        }
        return sb.toString();
    }
}
