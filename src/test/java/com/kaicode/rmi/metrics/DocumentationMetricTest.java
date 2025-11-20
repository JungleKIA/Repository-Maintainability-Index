package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;
import com.kaicode.rmi.model.RepositoryInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentationMetricTest {

    @Mock
    private GitHubClient client;

    private DocumentationMetric metric;

    @BeforeEach
    void setUp() {
        metric = new DocumentationMetric();
    }

    @Test
    void shouldReturnPerfectScoreWhenAllFilesPresent() throws IOException {
        // Mock repository info for small repository (should do full checks)
        RepositoryInfo smallRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .stars(500)    // Not large
                .forks(50)     // Not large
                .openIssues(50) // Not large
                .build();
        when(client.getRepository("owner", "repo")).thenReturn(smallRepo);

        when(client.hasFile("owner", "repo", "README.md")).thenReturn(true);
        when(client.hasFile("owner", "repo", "CONTRIBUTING.md")).thenReturn(true);
        when(client.hasFile("owner", "repo", "LICENSE")).thenReturn(true);
        when(client.hasFile("owner", "repo", "CODE_OF_CONDUCT.md")).thenReturn(true);
        when(client.hasFile("owner", "repo", "CHANGELOG.md")).thenReturn(true);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isEqualTo(100.0);
        assertThat(result.getName()).isEqualTo("Documentation");
        assertThat(result.getWeight()).isEqualTo(0.20);
    }

    @Test
    void shouldReturnZeroScoreWhenNoFilesPresent() throws IOException {
        // Mock repository info for small repository
        RepositoryInfo smallRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .stars(100)
                .forks(10)
                .openIssues(5)
                .build();
        when(client.getRepository("owner", "repo")).thenReturn(smallRepo);

        when(client.hasFile("owner", "repo", "README.md")).thenReturn(false);
        when(client.hasFile("owner", "repo", "CONTRIBUTING.md")).thenReturn(false);
        when(client.hasFile("owner", "repo", "LICENSE")).thenReturn(false);
        when(client.hasFile("owner", "repo", "CODE_OF_CONDUCT.md")).thenReturn(false);
        when(client.hasFile("owner", "repo", "CHANGELOG.md")).thenReturn(false);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isEqualTo(0.0);
    }

    @Test
    void shouldReturnPartialScoreWhenSomeFilesPresent() throws IOException {
        // Mock repository info for small repository
        RepositoryInfo smallRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .stars(200)
                .forks(25)
                .openIssues(15)
                .build();
        when(client.getRepository("owner", "repo")).thenReturn(smallRepo);

        when(client.hasFile("owner", "repo", "README.md")).thenReturn(true);
        when(client.hasFile("owner", "repo", "CONTRIBUTING.md")).thenReturn(false);
        when(client.hasFile("owner", "repo", "LICENSE")).thenReturn(true);
        when(client.hasFile("owner", "repo", "CODE_OF_CONDUCT.md")).thenReturn(false);
        when(client.hasFile("owner", "repo", "CHANGELOG.md")).thenReturn(true);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isEqualTo(60.0);
        assertThat(result.getDetails()).contains("README.md");
        assertThat(result.getDetails()).contains("LICENSE");
        assertThat(result.getDetails()).contains("CHANGELOG.md");
    }

    @Test
    void shouldHaveCorrectMetadata() {
        assertThat(metric.getMetricName()).isEqualTo("Documentation");
        assertThat(metric.getWeight()).isEqualTo(0.20);
    }

    @Test
    void shouldUseFastPathForMicrosoftVscode() throws IOException {
        MetricResult result = metric.calculate(client, "microsoft", "vscode");

        assertThat(result.getScore()).isEqualTo(80.0); // FAST_PATH score
        assertThat(result.getName()).isEqualTo("Documentation");
        assertThat(result.getWeight()).isEqualTo(0.20);
        assertThat(result.getDetails()).contains("Assumed present for large repository");
        // No file checks should be performed for FAST_PATH
        verify(client, never()).hasFile(anyString(), anyString(), anyString());
        verify(client, never()).getRepository(anyString(), anyString()); // FAST_PATH doesn't call getRepository
    }

    @Test
    void shouldUseFastModeForLargeRepositoryByStars() throws IOException {
        // Mock repository with >= 10000 stars (FAST_MODE case)
        RepositoryInfo largeRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("large-repo")
                .stars(15000)  // Triggers FAST_MODE
                .forks(2000)
                .openIssues(500)
                .build();
        when(client.getRepository("owner", "large-repo")).thenReturn(largeRepo);

        MetricResult result = metric.calculate(client, "owner", "large-repo");

        assertThat(result.getScore()).isEqualTo(80.0);
        assertThat(result.getDetails()).contains("Assumed present for large repository");
        verify(client, never()).hasFile(anyString(), anyString(), anyString());
    }

    @Test
    void shouldUseFastModeForLargeRepositoryByIssues() throws IOException {
        // Mock repository with >= 1000 open issues (FAST_MODE case)
        RepositoryInfo issueHeavyRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("busy-repo")
                .stars(5000)
                .forks(3000)
                .openIssues(5000)  // Triggers FAST_MODE
                .build();
        when(client.getRepository("owner", "busy-repo")).thenReturn(issueHeavyRepo);

        MetricResult result = metric.calculate(client, "owner", "busy-repo");

        assertThat(result.getScore()).isEqualTo(80.0);
        assertThat(result.getDetails()).contains("Assumed present for large repository");
        verify(client, never()).hasFile(anyString(), anyString(), anyString());
    }

    @Test
    void shouldUseFastModeForLargeRepositoryByForks() throws IOException {
        // Mock repository with >= 5000 forks (FAST_MODE case)
        RepositoryInfo forkHeavyRepo = RepositoryInfo.builder()
                .owner("owner")
                .name("forked-repo")
                .stars(3000)
                .forks(8000)  // Triggers FAST_MODE
                .openIssues(200)
                .build();
        when(client.getRepository("owner", "forked-repo")).thenReturn(forkHeavyRepo);

        MetricResult result = metric.calculate(client, "owner", "forked-repo");

        assertThat(result.getScore()).isEqualTo(80.0);
        assertThat(result.getDetails()).contains("Assumed present for large repository");
        verify(client, never()).hasFile(anyString(), anyString(), anyString());
    }
}
