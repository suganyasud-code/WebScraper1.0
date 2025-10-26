package com.example.jirascraper;

import org.json.JSONObject;

public class JiraIssuePromptBuilder {
    private static final int MAX_COMMENT_CHARS = 2000; // optional truncation

    /**
     * Builds a clean AI summarization prompt from a transformed JIRA issue JSON.
     *
     * @param issueJson Transformed JIRA issue JSON
     * @return AI-ready text string
     */
    public  String buildSummarizationPrompt(JSONObject issueJson) {
        StringBuilder prompt = new StringBuilder();

        // Title / summary
        String summary = issueJson.optString("summary", "").trim();
        if (!summary.isEmpty()) {
            prompt.append("Title: ").append(summary).append("\n");
        }

        // Description
        String description = issueJson.optString("description", "").trim();
        if (!description.isEmpty()) {
            prompt.append("Description: ").append(description).append("\n");
        }

        // Comments (optional truncation)
        String comments = issueJson.optString("commentsText", "").trim();
        if (!comments.isEmpty()) {
            if (comments.length() > MAX_COMMENT_CHARS) {
                comments = comments.substring(0, MAX_COMMENT_CHARS) + "...";
            }
            prompt.append("Comments: ").append(comments).append("\n");
        }

        // Optional context fields
        String labels = issueJson.optJSONArray("labels") != null
                ? issueJson.getJSONArray("labels").toString()
                : "";
        if (!labels.isEmpty()) {
            prompt.append("Labels: ").append(labels).append("\n");
        }

        String assignee = issueJson.optString("assignee", "").trim();
        if (!assignee.isEmpty()) {
            prompt.append("Assignee: ").append(assignee).append("\n");
        }

        String status = issueJson.optString("status", "").trim();
        if (!status.isEmpty()) {
            prompt.append("Status: ").append(status).append("\n");
        }

        // Instruction for AI
        prompt.append("\nPlease summarize this JIRA issue in 2-3 concise sentences, highlighting the main problem, actions taken, and outcome.");

        return prompt.toString().trim();
    }

    public  String buildClassificationPrompt(JSONObject issueJson) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Classify the following Jira issue into one of the categories: Bug, Improvement, Task, Feature, Documentation.\n\n");

        String summary = issueJson.optString("summary", "").trim();
        if (!summary.isEmpty()) {
            prompt.append("Title: ").append(summary).append("\n");
        }

        // Description
        String description = issueJson.optString("description", "").trim();
        if (!description.isEmpty()) {
            prompt.append("Description: ").append(description).append("\n");
        }

        prompt.append("\nProvide only the category as the output.");
        return prompt.toString().trim();
    }
}
