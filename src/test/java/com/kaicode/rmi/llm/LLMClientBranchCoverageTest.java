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

class LLMClientBranchCoverageTest {

    private MockWebServer mockServer;
    private LLMClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        
        String baseUrl = mockServer.url("/").toString().replaceAll("/$", "");
        client = new LLMClient(new OkHttpClient(), "test-key", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void shouldHandleNullErrorBody() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500));

        assertThatThrownBy(() -> client.analyze("Test"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("500");
    }

    @Test
    void shouldHandleEmptyResponse() {
        mockServer.enqueue(new MockResponse()
                .setBody("")
                .setResponseCode(500));

        assertThatThrownBy(() -> client.analyze("Test"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void shouldSendCorrectHeaders() throws Exception {
        String jsonResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "Response"
                        }
                    }]
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        client.analyze("Test");

        var request = mockServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-key");
        assertThat(request.getHeader("HTTP-Referer")).isNotEmpty();
        assertThat(request.getHeader("X-Title")).isNotEmpty();
    }

    @Test
    void shouldSendCorrectJsonPayload() throws Exception {
        String jsonResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "Response"
                        }
                    }]
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        client.analyze("Test prompt");

        var request = mockServer.takeRequest();
        String body = request.getBody().readUtf8();
        
        assertThat(body).contains("test-model");
        assertThat(body).contains("Test prompt");
        assertThat(body).contains("temperature");
        assertThat(body).contains("max_tokens");
    }

    @Test
    void shouldHandle403Error() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("{\"error\": \"Forbidden\"}"));

        assertThatThrownBy(() -> client.analyze("Test"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("403");
    }

    @Test
    void shouldHandle404Error() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\": \"Not found\"}"));

        assertThatThrownBy(() -> client.analyze("Test"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("404");
    }

    @Test
    void shouldHandle429RateLimit() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("{\"error\": \"Rate limited\"}"));

        assertThatThrownBy(() -> client.analyze("Test"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("429");
    }

    @Test
    void shouldHandleInvalidJson() {
        mockServer.enqueue(new MockResponse()
                .setBody("invalid json")
                .setResponseCode(200));

        assertThatThrownBy(() -> client.analyze("Test"))
                .isInstanceOf(Exception.class);
    }
}
