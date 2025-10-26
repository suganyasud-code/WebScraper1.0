package com.example.jirascraper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JiraClient {
    private  String BASE_URL;
    private  int CONNECT_TIMEOUT = 10000; // 10 sec
    private  int READ_TIMEOUT = 15000;    // 15 sec

    // Optional: configure rate limit pause (in ms)
    private static final long RATE_LIMIT_SLEEP = 2000;

    public JiraClient(String jiraBaseUrl) {
        this.BASE_URL = jiraBaseUrl;
    }

    /** Make a GET request and return the JSON response */
    public JSONObject get(String endpoint) throws IOException, JSONException {
        String urlString = BASE_URL + endpoint;
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();

            // Handle rate limiting or retryable errors
            if (status == 429) {
                System.out.println("Rate limit hit. Sleeping for a bit...");
                try {
                    Thread.sleep(RATE_LIMIT_SLEEP);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return get(endpoint); // retry
            }

            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + status);
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return new JSONObject(response.toString());
        } finally {
            if (reader != null) reader.close();
            if (conn != null) conn.disconnect();
        }
    }
}
