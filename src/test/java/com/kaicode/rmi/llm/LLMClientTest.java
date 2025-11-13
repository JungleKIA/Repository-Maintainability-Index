package com.kaicode.rmi.llm;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LLMClientTest {

    private MockWebServer mockServer;
    private LLMClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        
        String baseUrl = mockServer.url("/").toString().replaceAll("/$", "");
        client = new LLMClient("test-key", "test-model", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void shouldAnalyzeSuccessfully() throws Exception {
        String jsonResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "Test response"
                        }
                    }],
                    "usage": {
                        "total_tokens": 100
                    }
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        LLMClient.LLMResponse response = client.analyze("Test prompt");

        assertThat(response.getContent()).isEqualTo("Test response");
        assertThat(response.getTokensUsed()).isEqualTo(100);
    }

    @Test
    void shouldHandleApiError() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\": \"Unauthorized\"}"));

        assertThatThrownBy(() -> client.analyze("Test"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("401");
    }

    @Test
    void shouldHandleResponseWithoutUsage() throws Exception {
        String jsonResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "Test"
                        }
                    }]
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        LLMClient.LLMResponse response = client.analyze("Test");

        assertThat(response.getContent()).isEqualTo("Test");
        assertThat(response.getTokensUsed()).isEqualTo(0);
    }

    @Test
    void shouldUseDefaultApiUrl() {
        LLMClient defaultClient = new LLMClient("key", "model");
        assertThat(defaultClient).isNotNull();
    }

    // ========== NEW TESTS FOR TEXT CLEANING ==========

    @Test
    void shouldCleanMojibakeFromResponse() throws Exception {
        // Mock API response with mojibake patterns
        String jsonResponse = "{\"choices\":[{\"message\":{\"content\":\"Well-structured sections with ΓöÇΓöÇΓöÇ separators\"}}],\"usage\":{\"total_tokens\":50}}";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        LLMClient.LLMResponse response = client.analyze("Test prompt");

        // Verify mojibake is cleaned
        assertThat(response.getContent()).doesNotContain("ΓöÇ");
        assertThat(response.getContent()).contains("Well-structured", "───");
    }

    @Test
    void shouldCleanBoxDrawingCharacterMojibake() throws Exception {
        String jsonResponse = "{\"choices\":[{\"message\":{\"content\":\"ΓòÉΓòÉΓòÉ Header ΓòÉΓòÉΓòÉ\"}}],\"usage\":{\"total_tokens\":30}}";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        LLMClient.LLMResponse response = client.analyze("Test");

        // Verify box-drawing mojibake is cleaned
        assertThat(response.getContent()).doesNotContain("ΓòÉ");
        assertThat(response.getContent()).contains("═══");
    }

    @Test
    void shouldCleanDashVariantMojibake() throws Exception {
        String jsonResponse = "{\"choices\":[{\"message\":{\"content\":\"firstΓÇæresponse time 24ΓÇô48 hours\"}}],\"usage\":{\"total_tokens\":40}}";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        LLMClient.LLMResponse response = client.analyze("Test");

        // Verify dash variant mojibake is cleaned
        assertThat(response.getContent()).doesNotContain("ΓÇæ", "ΓÇô");
        assertThat(response.getContent()).isEqualTo("first-response time 24-48 hours");
    }

    @Test
    void shouldCleanMultipleMojibakePatterns() throws Exception {
        String jsonResponse = "{\"choices\":[{\"message\":{\"content\":\"ΓòÉΓòÉ Header ΓöÇΓöÇ Text Γû¬ Bullet firstΓÇæresponse\"}}],\"usage\":{\"total_tokens\":60}}";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        LLMClient.LLMResponse response = client.analyze("Test");

        // Verify all mojibake patterns are cleaned
        assertThat(response.getContent()).doesNotContain("ΓòÉ", "ΓöÇ", "Γû¬", "ΓÇæ");
        assertThat(response.getContent()).contains("═", "─", "▪", "-");
    }

    @Test
    void shouldHandleCleanTextWithoutMojibake() throws Exception {
        String jsonResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "Clean text without any mojibake"
                        }
                    }],
                    "usage": {
                        "total_tokens": 20
                    }
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        LLMClient.LLMResponse response = client.analyze("Test");

        // Clean text should remain unchanged
        assertThat(response.getContent()).isEqualTo("Clean text without any mojibake");
    }

    @Test
    void shouldCleanEmptyResponse() throws Exception {
        String jsonResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": ""
                        }
                    }],
                    "usage": {
                        "total_tokens": 0
                    }
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        LLMClient.LLMResponse response = client.analyze("Test");

        // Empty content should be handled gracefully
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    void shouldCleanComplexLLMResponse() throws Exception {
        String jsonResponse = "{\"choices\":[{\"message\":{\"content\":\"Well-structured sections with clear headings\\nComprehensive links to external resources\\nIncrease the frequency and speed of maintainer responses; many issues currently show no reply or followΓÇæup.\"}}],\"usage\":{\"total_tokens\":100}}";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        LLMClient.LLMResponse response = client.analyze("Test");

        // Verify complex mojibake is cleaned
        assertThat(response.getContent()).doesNotContain("ΓÇæ");
        assertThat(response.getContent()).contains("Well-structured", "follow-up");
    }
}
