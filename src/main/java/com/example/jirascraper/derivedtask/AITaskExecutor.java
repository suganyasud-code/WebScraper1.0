package com.example.jirascraper.derivedtask;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.util.Arrays;

public class AITaskExecutor {
    private final OpenAiService openAiService;
    private final String modelName;

    public AITaskExecutor(String apiKey, String modelName) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        this.modelName = modelName != null ? modelName : "gpt-3.5-turbo";
    }

    public String execute(String prompt) {
        ChatMessage systemMessage = new ChatMessage("system",
                "You are a helpful assistant that summarizes and classifies Jira issues in a concise, clear way.");

        ChatMessage userMessage = new ChatMessage("user", prompt);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(Arrays.asList(systemMessage, userMessage))
                .maxTokens(250)
                .temperature(0.5)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);
        return result.getChoices().get(0).getMessage().getContent().trim();
    }

    public void shutdown() {
        openAiService.shutdownExecutor();
    }
}
