package com.kaicode.rmi.github;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitHubClientBranchCoverageTest {

    private MockWebServer mockServer;
    private GitHubClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        String baseUrl = mockServer.url("/").toString().replaceAll("/$", "");
        client = new GitHubClient(httpClient, "test-token", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }



    @Test
    void shouldHandleApiErrorWithEmptyBody() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        assertThatThrownBy(() -> client.getRepository("owner", "repo"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("404");
    }

    @Test
    void shouldHandleApiErrorWithNullBody() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(403));

        assertThatThrownBy(() -> client.getRepository("owner", "repo"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("403");
    }

    @Test
    void shouldHandleUnauthorizedError() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"message\": \"Bad credentials\"}"));

        assertThatThrownBy(() -> client.getRepository("owner", "repo"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("401");
    }

    @Test
    void shouldHandleRateLimitError() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("{\"message\": \"API rate limit exceeded\"}"));

        assertThatThrownBy(() -> client.getRepository("owner", "repo"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("403");
    }

    @Test
    void shouldHandleServerError() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setBody("Service Unavailable"));

        assertThatThrownBy(() -> client.getBranchCount("owner", "repo"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("503");
    }

    @Test
    void shouldHandleEmptyBranchList() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("[]")
                .setResponseCode(200));

        int count = client.getBranchCount("owner", "repo");
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldHandleEmptyContributorList() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("[]")
                .setResponseCode(200));

        int count = client.getContributorCount("owner", "repo");
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldHandleMaxCommitLimit() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("[]")
                .setResponseCode(200));

        client.getRecentCommits("owner", "repo", 150);

        var request = mockServer.takeRequest();
        assertThat(request.getPath()).contains("per_page=100");
    }

    @Test
    void shouldHandleExactCommitLimit() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("[]")
                .setResponseCode(200));

        client.getRecentCommits("owner", "repo", 100);

        var request = mockServer.takeRequest();
        assertThat(request.getPath()).contains("per_page=100");
    }

    @Test
    void shouldHandleSmallCommitCount() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("[]")
                .setResponseCode(200));

        client.getRecentCommits("owner", "repo", 5);

        var request = mockServer.takeRequest();
        assertThat(request.getPath()).contains("per_page=5");
    }
}
