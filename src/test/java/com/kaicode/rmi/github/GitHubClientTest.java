package com.kaicode.rmi.github;

import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.RepositoryInfo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitHubClientTest {

    private MockWebServer mockServer;
    private GitHubClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .build();
        
        String baseUrl = mockServer.url("/").toString().replaceAll("/$", "");
        client = new GitHubClient(httpClient, "test-token", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void shouldFetchRepositoryInfo() throws Exception {
        String jsonResponse = """
                {
                    "name": "test-repo",
                    "owner": {"login": "test-owner"},
                    "description": "Test description",
                    "stargazers_count": 100,
                    "forks_count": 20,
                    "open_issues_count": 5,
                    "updated_at": "2024-01-01T12:00:00Z",
                    "has_wiki": true,
                    "has_issues": true,
                    "default_branch": "main",
                    "size": 1024
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        RepositoryInfo repo = client.getRepository("test-owner", "test-repo");

        assertThat(repo.getOwner()).isEqualTo("test-owner");
        assertThat(repo.getName()).isEqualTo("test-repo");
        assertThat(repo.getDescription()).isEqualTo("Test description");
        assertThat(repo.getStars()).isEqualTo(100);
        assertThat(repo.getForks()).isEqualTo(20);
        assertThat(repo.getOpenIssues()).isEqualTo(5);

        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-token");
    }

    @Test
    void shouldFetchRecentCommits() throws Exception {
        String jsonResponse = """
                [
                    {
                        "sha": "abc123",
                        "commit": {
                            "message": "feat: add new feature",
                            "author": {
                                "name": "John Doe",
                                "date": "2024-01-01T12:00:00Z"
                            }
                        }
                    },
                    {
                        "sha": "def456",
                        "commit": {
                            "message": "fix: bug fix",
                            "author": {
                                "name": "Jane Smith",
                                "date": "2024-01-02T12:00:00Z"
                            }
                        }
                    }
                ]
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        List<CommitInfo> commits = client.getRecentCommits("owner", "repo", 10);

        assertThat(commits).hasSize(2);
        assertThat(commits.get(0).getSha()).isEqualTo("abc123");
        assertThat(commits.get(0).getMessage()).isEqualTo("feat: add new feature");
        assertThat(commits.get(0).getAuthor()).isEqualTo("John Doe");
        assertThat(commits.get(1).getSha()).isEqualTo("def456");
    }

    @Test
    void shouldReturnTrueWhenFileExists() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("{}")
                .setResponseCode(200));

        boolean hasFile = client.hasFile("owner", "repo", "README.md");

        assertThat(hasFile).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFileDoesNotExist() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404));

        boolean hasFile = client.hasFile("owner", "repo", "MISSING.md");

        assertThat(hasFile).isFalse();
    }

    @Test
    void shouldThrowExceptionOnApiError() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        assertThatThrownBy(() -> client.getRepository("owner", "repo"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("500");
    }

    @Test
    void shouldGetBranchCount() throws Exception {
        String jsonResponse = """
                [
                    {"name": "main"},
                    {"name": "develop"},
                    {"name": "feature"}
                ]
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        int branchCount = client.getBranchCount("owner", "repo");

        assertThat(branchCount).isEqualTo(3);
    }

    @Test
    void shouldGetContributorCount() throws Exception {
        String jsonResponse = """
                [
                    {"login": "user1"},
                    {"login": "user2"}
                ]
                """;

        mockServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        int contributorCount = client.getContributorCount("owner", "repo");

        assertThat(contributorCount).isEqualTo(2);
    }
}
