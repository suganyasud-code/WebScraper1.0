package com.example.jirascraper;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class IssueStorage {
    private final File outputFile;

    public IssueStorage(String path) {
        this.outputFile = new File(path);
    }

    /** Append each issue as JSON per line */
    public void appendIssue(JSONObject issueJson) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            writer.write(issueJson.toString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write issue: " + e.getMessage());
        }
    }
}
