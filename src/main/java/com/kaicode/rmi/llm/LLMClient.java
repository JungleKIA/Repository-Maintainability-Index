package com.kaicode.rmi.llm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for Large Language Model (LLM) API integration.
 * <p>
 * This class provides HTTP communication with OpenRouter API for accessing various
 * LLM services. It handles request construction, response parsing, error handling,
 * and provides transparent token usage tracking for cost management.
 * <p>
 * The client supports:
 * <ul>
 *   <li>Configurable API endpoints and models</li>
 *   <li>Automatic request/response formatting via JSON</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Cost tracking through token usage reporting</li>
 *   <li>Timeout configuration for reliable network operations</li>
 * </ul>
 * <p>
 * Designed for integration with {@link LLMAnalyzer} to provide AI-powered
 * repository analysis capabilities with enterprise-grade reliability.
 *
 * @since 1.0
 * @see LLMAnalyzer
 * @see LLMResponse
 */
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

    /**
     * Creates a new LLM client with default OpenRouter API endpoint.
     * <p>
     * Initializes HTTP client with standard configuration and connects to
     * the default OpenRouter API endpoint for multi-model LLM access.
     *
     * @param apiKey API key for authentication with the LLM service, must not be null
     * @param model the specific LLM model to use (e.g., "gpt-3.5-turbo", "claude-3-haiku"), must not be null
     */
    public LLMClient(String apiKey, String model) {
        this(apiKey, model, DEFAULT_API_URL);
    }

    /**
     * Creates a new LLM client with custom API endpoint.
     * <p>
     * Provides full control over API endpoint for flexibility in LLM provider
     * selection or custom deployments. HTTP client is configured with production-ready
     * timeouts and connection settings.
     *
     * @param apiKey API key for authentication, must not be null
     * @param model LLM model identifier, must not be null
     * @param apiUrl custom API endpoint URL, must not be null
     * @since 1.0
     */
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

    /**
     * Internal constructor for testing with mock HTTP client.
     * <p>
     * Used in unit tests to provide dependency injection for HTTP client mocking.
     * Allows complete isolation of network dependencies during testing.
     *
     * @param httpClient the HTTP client instance to use
     * @param apiKey API key for authentication
     * @param apiUrl API endpoint URL
     */
    LLMClient(OkHttpClient httpClient, String apiKey, String apiUrl) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.model = "test-model";
        this.apiUrl = apiUrl;
        this.gson = new Gson();
    }

    /**
     * Sends a prompt to the LLM and returns the generated response.
     * <p>
     * Performs complete HTTP request cycle: constructs OpenAI-compatible JSON request,
     * sends to configured endpoint, handles response parsing and error cases.
     * Includes performance tracking and automatic JSON structure validation.
     * <p>
     * Request configuration:
     * <ul>
     *   <li>Temperature: 0.3 (balanced creativity/accuracy)</li>
     *   <li>Max tokens: 2000 (comprehensive responses)</li>
     *   <li>Role: user (direct instruction format)</li>
     * </ul>
     * <p>
     * Error handling includes detailed HTTP status codes and response bodies
     * for comprehensive debugging capabilities.
     *
     * @param prompt the text prompt to send to the LLM, must not be null
     * @return LLM response containing generated content and token usage, never null
     * @throws IOException if network failure, authentication error, or API rejection occurs
     * @since 1.0
     * @see LLMResponse
     */
    /**
     * Gets the LLM model identifier used by this client.
     * <p>
     * Returns the specific model name or identifier that this client uses
     * for all LLM API requests (e.g., "openai/gpt-oss-20b:free").
     *
     * @return the model identifier string, never null
     */
    public String getModel() {
        return model;
    }

    public LLMResponse analyze(String prompt) throws IOException {
        logger.info("Sending LLM request to model: {}", model);

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

                // Enhanced error logging with model and provider info
                logger.warn("LLM API request failed for model '{}': HTTP {} - {}",
                    model, response.code(), extractErrorMessage(errorBody));

                throw new IOException(String.format("LLM API request failed: %d (model: %s) - %s",
                        response.code(), model, errorBody));
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

            // Note: Text cleaning is done in LLMAnalyzer after JSON parsing
            // to avoid breaking JSON structure

            int tokensUsed = 0;
            if (jsonResponse.has("usage")) {
                tokensUsed = jsonResponse.getAsJsonObject("usage")
                        .get("total_tokens")
                        .getAsInt();
            }

            return new LLMResponse(content, tokensUsed);
        }
    }

    /**
     * Extracts a clean, readable error message from the raw API error response.
     * <p>
     * Handles OpenRouter API error format and extracts the most relevant error information
     * for better logging and debugging. Falls back to the full response if extraction fails.
     *
     * @param errorBody the raw error response body from the API
     * @return a clean, readable error message suitable for logging
     */
    private String extractErrorMessage(String errorBody) {
        if (errorBody == null || errorBody.trim().isEmpty()) {
            return "No error details available";
        }

        try {
            // Try to parse as JSON and extract meaningful error message
            JsonObject errorJson = gson.fromJson(errorBody, JsonObject.class);

            if (errorJson.has("error")) {
                JsonObject errorObj = errorJson.getAsJsonObject("error");

                // Extract main error message
                if (errorObj.has("message")) {
                    String message = errorObj.get("message").getAsString();

                    // Add code if available
                    if (errorObj.has("code")) {
                        int code = errorObj.get("code").getAsInt();
                        message = String.format("[%d] %s", code, message);
                    }

                    return message;
                }
            }
        } catch (Exception e) {
            // If JSON parsing fails, return original or truncated version
            logger.debug("Failed to parse error body as JSON: {}", e.getMessage());
        }

        // Fallback: return truncated version of the raw error
        return errorBody.length() > 200 ?
            errorBody.substring(0, 200) + "..." : errorBody;
    }

    /**
     * Response container for LLM API calls.
     * <p>
     * Encapsulates the results of LLM analysis including generated text content
     * and token usage metrics for cost tracking and performance monitoring.
     * Instances are immutable and thread-safe.
     * <p>
     * Token usage provides valuable insights into computational cost and
     * enables optimization of prompt engineering and model selection.
     *
     * @since 1.0
     */
    public static class LLMResponse {
        private final String content;
        private final int tokensUsed;

        /**
         * Creates a new LLM response container.
         * <p>
         * Initializes the response with generated content and consumption metrics.
         *
         * @param content the text content generated by the LLM, may be null
         * @param tokensUsed total number of tokens consumed in the request, non-negative
         */
        public LLMResponse(String content, int tokensUsed) {
            this.content = content;
            this.tokensUsed = tokensUsed;
        }

        /**
         * Gets the generated response content.
         * <p>
         * Returns the raw text content produced by the LLM.
         * Note: Encoding artifacts should be cleaned at a higher level
         * to avoid JSON parsing complications.
         *
         * @return the response content as generated by the LLM
         */
        public String getContent() {
            return content;
        }

        /**
         * Gets the total token usage for this request.
         * <p>
         * Reports the computational cost of the LLM request,
         * encompassing both input prompt and generated response.
         * Useful for cost monitoring, quota management, and performance analysis.
         *
         * @return total tokens consumed in the LLM request, always non-negative
         */
        public int getTokensUsed() {
            return tokensUsed;
        }
    }
}
