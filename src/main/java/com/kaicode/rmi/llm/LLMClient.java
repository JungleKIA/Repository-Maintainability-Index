package com.kaicode.rmi.llm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LLMClient {
    private static final Logger logger = LoggerFactory.getLogger(LLMClient.class);
    private static final String DEFAULT_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public LLMClient(String apiKey, String model) {
        this(apiKey, model, DEFAULT_API_URL);
    }

    public LLMClient(String apiKey, String model, String apiUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    LLMClient(OkHttpClient httpClient, String apiKey, String apiUrl) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.model = "test-model";
        this.apiUrl = apiUrl;
        this.gson = new Gson();
    }

    public LLMResponse analyze(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 2000);

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", "https://github.com/kaicode/rmi")
                .header("X-Title", "Repository Maintainability Index")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new IOException(String.format("LLM API request failed: %d - %s", 
                        response.code(), errorBody));
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            String content = jsonResponse
                    .getAsJsonArray("choices")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();

            // Clean text from mojibake artifacts before returning
            String cleanedContent = com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows(content);

            int tokensUsed = 0;
            if (jsonResponse.has("usage")) {
                tokensUsed = jsonResponse.getAsJsonObject("usage")
                        .get("total_tokens")
                        .getAsInt();
            }

            return new LLMResponse(cleanedContent, tokensUsed);
        }
    }

    public static class LLMResponse {
        private final String content;
        private final int tokensUsed;

        public LLMResponse(String content, int tokensUsed) {
            this.content = content;
            this.tokensUsed = tokensUsed;
        }

        public String getContent() {
            return content;
        }

        public int getTokensUsed() {
            return tokensUsed;
        }
    }
}
