package com.kaicode.rmi.github;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubClientExtendedTest {

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
    void shouldHandleNullDescription() throws Exception {
        String repoResponse = """
                {
                    "name": "test-repo",
                    "owner": {"login": "test-owner"},
                    "description": null,
                    "stargazers_count": 0,
                    "forks_count": 0,
                    "open_issues_count": 0,
                    "updated_at": "2024-01-01T12:00:00Z",
                    "has_wiki": false,
                    "has_issues": false,
                    "default_branch": "main",
                    "size": 0
                }
                """;

        // Enqueue repo response
        mockServer.enqueue(new MockResponse()
                .setBody(repoResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        // Enqueue open issues response (empty)
        mockServer.enqueue(new MockResponse()
                .setBody("[]")
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        var repo = client.getRepository("test-owner", "test-repo");

        assertThat(repo.getDescription()).isEmpty();
    }

    @Test
    void shouldGetClosedIssuesCountWithPagination() throws Exception {
        String jsonResponsePage1 = "[{},{},{},{},{}]"; // 5 issues without pull_request
        String jsonResponsePage2 = "[]";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponsePage1)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponsePage2)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        int count = client.getClosedIssuesCount("owner", "repo");

        assertThat(count).isEqualTo(5);
    }

    @Test
    void shouldGetClosedIssuesCountWithoutLinkHeader() throws Exception {
        String jsonResponse = "[{}, {}, {}]";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        int count = client.getClosedIssuesCount("owner", "repo");

        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldGetClosedIssuesCountWithInvalidLinkHeader() throws Exception {
        String jsonResponse = "[{}, {}]";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .addHeader("Link", "<http://example.com>; rel=\"next\""));

        int count = client.getClosedIssuesCount("owner", "repo");

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldHandleEmptyCommitList() throws Exception {
        String jsonResponse = "[]";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        var commits = client.getRecentCommits("owner", "repo", 10);

        assertThat(commits).isEmpty();
    }

    @Test
    void shouldLimitCommitCountTo100() throws Exception {
        String jsonResponse = "[]";

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        client.getRecentCommits("owner", "repo", 200);

        var request = mockServer.takeRequest();
        assertThat(request.getPath()).contains("per_page=100");
    }

    @Test
    void shouldWorkWithoutToken() throws Exception {
        GitHubClient clientNoToken = new GitHubClient(new OkHttpClient(), null, 
                mockServer.url("/").toString().replaceAll("/$", ""));

        String jsonResponse = "[]";
        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        clientNoToken.getRecentCommits("owner", "repo", 10);

        var request = mockServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isNull();
    }

    @Test
    void shouldWorkWithEmptyToken() throws Exception {
        GitHubClient clientEmptyToken = new GitHubClient(new OkHttpClient(), "", 
                mockServer.url("/").toString().replaceAll("/$", ""));

        String jsonResponse = "[]";
        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200));

        clientEmptyToken.getRecentCommits("owner", "repo", 10);

        var request = mockServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isNull();
    }

    @Test
    void shouldUseDefaultApiBaseUrl() {
        GitHubClient defaultClient = new GitHubClient("token");
        assertThat(defaultClient).isNotNull();
    }

    @Test
    void shouldUseCustomApiBaseUrl() {
        GitHubClient customClient = new GitHubClient("token", "https://custom.api");
        assertThat(customClient).isNotNull();
    }
}
