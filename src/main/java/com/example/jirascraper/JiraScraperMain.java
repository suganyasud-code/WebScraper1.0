package com.example.jirascraper;

import com.example.jirascraper.derivedtask.AITaskExecutor;
import com.example.jirascraper.model.JiraIssue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;


public class JiraScraperMain {
    public static void main(String[] args) throws IOException {
        //Loads config from config file
        ConfigLoader config = new ConfigLoader("config.properties");
        String jiraBaseUrl = config.get("jira.baseUrl");
        String jiraProject = config.get("jira.project");
        int maxResults = config.getInt("jira.maxResults", 50);

        //Initialize Jiraclient and fetch issues
        JiraClient client = new JiraClient(jiraBaseUrl);
        JiraIssueFetcher fetcher = new JiraIssueFetcher(client,jiraProject,maxResults);
        CheckpointManager checkpoint = new CheckpointManager(config.get("path.checkpoint"));
        IssueStorage storage = new IssueStorage(config.get("path.output"));

        //Initialize AIPromptBuilder
        JiraIssuePromptBuilder promptBuilder = new JiraIssuePromptBuilder();
        String openAiApiKey = config.get("openai.api.key");
        String openAiModel = config.get("openai.model");
        AITaskExecutor aiTaskExecutor = new AITaskExecutor(openAiApiKey,openAiModel);

        int startAt = checkpoint.loadLastStartAt();
        System.out.println("Resuming from startAt=" + startAt);

        try {
            while (true) {
                JSONObject page = fetcher.fetchIssuesPage(startAt);
                JSONArray issues = page.getJSONArray("issues");
                if (issues.isEmpty()) break;

                for (int i = 0; i < issues.length(); i++) {
                    JSONObject rawIssue = issues.getJSONObject(i);
                    JiraIssue issue = fetcher.parseIssue(rawIssue);

                    // convert to JSON for persistence
                    JSONObject issueJson = new JSONObject()
                            .put("key", issue.key)
                            .put("summary", issue.summary)
                            .put("status", issue.status)
                            .put("priority", issue.priority)
                            .put("assignee", issue.assignee)
                            .put("labels", issue.labels)
                            .put("created", issue.created)
                            .put("updated", issue.updated)
                            .put("description",issue.description)
                            .put("commentText", issue.commentsText);
                    //Summarize the JIRA issue by constructing the prompt using JIRA metadata
                    String summarizationPrompt = promptBuilder.buildSummarizationPrompt(issueJson);
                    String classificationPrompt = promptBuilder.buildClassificationPrompt(issueJson);
                    issueJson.put("AISummary", aiTaskExecutor.execute(summarizationPrompt));
                    issueJson.put("category",aiTaskExecutor.execute(classificationPrompt));
                    storage.appendIssue(issueJson);
                }

                startAt += issues.length();
                checkpoint.saveLastStartAt(startAt);
                System.out.println("Fetched up to " + startAt);

                if (startAt >= page.getInt("total")) break;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            checkpoint.saveLastStartAt(startAt); // save progress even on failure
        }

        System.out.println("Scraping complete ✅");
    }
}