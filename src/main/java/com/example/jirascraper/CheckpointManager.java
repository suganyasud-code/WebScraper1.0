package com.example.jirascraper;

import org.json.JSONObject;

import java.io.*;

public class CheckpointManager {
    private final File checkpointFile;

    public CheckpointManager(String path) {
        this.checkpointFile = new File(path);
    }

    /** Load last saved state */
    public int loadLastStartAt() {
        if (!checkpointFile.exists()) return 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(checkpointFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            JSONObject obj = new JSONObject(sb.toString());
            return obj.optInt("lastFetchedStartAt", 0);
        } catch (Exception e) {
            System.err.println("Failed to read checkpoint: " + e.getMessage());
            return 0;
        }
    }

    /** Save current progress */
    public void saveLastStartAt(int startAt) {
        JSONObject obj = new JSONObject();
        obj.put("lastFetchedStartAt", startAt);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(checkpointFile))) {
            writer.write(obj.toString());
        } catch (IOException e) {
            System.err.println("Failed to save checkpoint: " + e.getMessage());
        }
    }
}
