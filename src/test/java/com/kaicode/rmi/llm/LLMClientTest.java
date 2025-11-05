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
}
