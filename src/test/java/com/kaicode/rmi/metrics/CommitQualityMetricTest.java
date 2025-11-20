package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.MetricResult;
import com.kaicode.rmi.model.RepositoryInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommitQualityMetricTest {

    @Mock
    private GitHubClient client;

    private CommitQualityMetric metric;

    @BeforeEach
    void setUp() {
        metric = new CommitQualityMetric();
    }

    @Test
    void shouldReturnHighScoreForGoodCommits() throws IOException {
        // Mock small repository info (should analyze 50 commits)
        RepositoryInfo smallRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .stars(200)
                .forks(20)
                .openIssues(10)
                .build();
        when(client.getRepository("owner", "repo")).thenReturn(smallRepo);

        List<CommitInfo> commits = List.of(
                createCommit("feat: add new feature"),
                createCommit("fix: resolve bug"),
                createCommit("docs: update README"),
                createCommit("refactor: improve code structure")
        );

        when(client.getRecentCommits("owner", "repo", 50)).thenReturn(commits);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isEqualTo(100.0);
        assertThat(result.getName()).isEqualTo("Commit Quality");
        assertThat(result.getWeight()).isEqualTo(0.15);
    }

    @Test
    void shouldReturnLowScoreForPoorCommits() throws IOException {
        // Mock small repository info
        RepositoryInfo smallRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .stars(100)
                .forks(10)
                .openIssues(5)
                .build();
        when(client.getRepository("owner", "repo")).thenReturn(smallRepo);

        List<CommitInfo> commits = List.of(
                createCommit("fix"),
                createCommit("wip"),
                createCommit("update"),
                createCommit("test")
        );

        when(client.getRecentCommits("owner", "repo", 50)).thenReturn(commits);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isLessThan(50.0);
    }

    @Test
    void shouldReturnZeroScoreForNoCommits() throws IOException {
        // Mock small repository info
        RepositoryInfo smallRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .stars(50)
                .forks(5)
                .openIssues(2)
                .build();
        when(client.getRepository("owner", "repo")).thenReturn(smallRepo);

        when(client.getRecentCommits("owner", "repo", 50)).thenReturn(List.of());

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isEqualTo(0.0);
        assertThat(result.getDetails()).contains("No commits");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "feat: add feature",
            "fix: bug fix",
            "docs: documentation",
            "style: formatting",
            "refactor: code improvement",
            "test: add tests",
            "chore: maintenance",
            "perf: performance",
            "ci: continuous integration",
            "build: build system"
    })
    void shouldRecognizeConventionalCommits(String message) {
        assertThat(metric.isGoodCommit(message)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "wip",
            "fix",
            "update",
            "",
            "merge branch"
    })
    void shouldRejectPoorCommits(String message) {
        assertThat(metric.isGoodCommit(message)).isFalse();
    }

    @Test
    void shouldAcceptDescriptiveCommitsWithoutConvention() {
        assertThat(metric.isGoodCommit("Implement user authentication with JWT tokens"))
                .isTrue();
    }

    @Test
    void shouldRejectShortCommits() {
        assertThat(metric.isGoodCommit("Short msg")).isFalse();
    }

    @Test
    void shouldRejectNullOrEmptyCommits() {
        assertThat(metric.isGoodCommit(null)).isFalse();
        assertThat(metric.isGoodCommit("")).isFalse();
        assertThat(metric.isGoodCommit("   ")).isFalse();
    }

    @Test
    void shouldUseReducedCommitSampleForLargeRepository() throws IOException {
        // Mock large repository (stars >= 10000)
        RepositoryInfo largeRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("large-repo")
                .stars(15000)  // Triggers large repo detection
                .forks(5000)
                .openIssues(1000)
                .build();
        when(client.getRepository("owner", "large-repo")).thenReturn(largeRepo);

        List<CommitInfo> commits = List.of(
                createCommit("feat: add new feature"),
                createCommit("fix: resolve bug"),
                createCommit("docs: update README")
        );

        // For large repos, should ask for only 10 commits instead of 50
        when(client.getRecentCommits("owner", "large-repo", 10)).thenReturn(commits);

        MetricResult result = metric.calculate(client, "owner", "large-repo");

        assertThat(result.getScore()).isEqualTo(100.0);
        assertThat(result.getName()).isEqualTo("Commit Quality");
        assertThat(result.getDetails()).contains("sampled for large repository");
    }

    @Test
    void shouldUseFullCommitSampleForSmallRepository() throws IOException {
        // Mock small repository
        RepositoryInfo smallRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("small-repo")
                .stars(500)  // Not large
                .forks(50)
                .openIssues(20)
                .build();
        when(client.getRepository("owner", "small-repo")).thenReturn(smallRepo);

        List<CommitInfo> commits = List.of(
                createCommit("feat: add new feature"),
                createCommit("fix: resolve bug"),
                createCommit("docs: update README")
        );

        // For small repos, should ask for full 50 commits
        when(client.getRecentCommits("owner", "small-repo", 50)).thenReturn(commits);

        MetricResult result = metric.calculate(client, "owner", "small-repo");

        assertThat(result.getScore()).isEqualTo(100.0);
        assertThat(result.getDetails()).doesNotContain("commit sampling for large repository");
    }

    private CommitInfo createCommit(String message) {
        return CommitInfo.builder()
                .sha("abc123")
                .message(message)
                .author("Test Author")
                .date(LocalDateTime.now())
                .build();
    }
}
