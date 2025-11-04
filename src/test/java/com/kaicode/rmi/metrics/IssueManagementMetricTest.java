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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueManagementMetricTest {

    @Mock
    private GitHubClient client;

    private IssueManagementMetric metric;

    @BeforeEach
    void setUp() {
        metric = new IssueManagementMetric();
    }

    @Test
    void shouldReturnHighScoreForGoodClosureRate() throws IOException {
        RepositoryInfo repo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .hasIssues(true)
                .openIssues(10)
                .build();

        when(client.getRepository("owner", "repo")).thenReturn(repo);
        when(client.getClosedIssuesCount("owner", "repo")).thenReturn(90);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isGreaterThan(90.0);
        assertThat(result.getName()).isEqualTo("Issue Management");
    }

    @Test
    void shouldReturnLowScoreForPoorClosureRate() throws IOException {
        RepositoryInfo repo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .hasIssues(true)
                .openIssues(90)
                .build();

        when(client.getRepository("owner", "repo")).thenReturn(repo);
        when(client.getClosedIssuesCount("owner", "repo")).thenReturn(10);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isLessThan(50.0);
    }

    @Test
    void shouldReturnModerateScoreWhenIssuesDisabled() throws IOException {
        RepositoryInfo repo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .hasIssues(false)
                .build();

        when(client.getRepository("owner", "repo")).thenReturn(repo);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isEqualTo(50.0);
        assertThat(result.getDetails()).contains("disabled");
    }

    @Test
    void shouldReturnModerateScoreWhenNoIssues() throws IOException {
        RepositoryInfo repo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .hasIssues(true)
                .openIssues(0)
                .build();

        when(client.getRepository("owner", "repo")).thenReturn(repo);
        when(client.getClosedIssuesCount("owner", "repo")).thenReturn(0);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isEqualTo(80.0);
    }

    @Test
    void shouldCalculateScoreCorrectly() {
        assertThat(metric.calculateScore(90, 5)).isGreaterThanOrEqualTo(95.0);
        assertThat(metric.calculateScore(70, 5)).isGreaterThanOrEqualTo(80.0);
        assertThat(metric.calculateScore(50, 5)).isGreaterThanOrEqualTo(65.0);
        assertThat(metric.calculateScore(30, 5)).isGreaterThanOrEqualTo(45.0);
        assertThat(metric.calculateScore(10, 5)).isGreaterThanOrEqualTo(27.0);
    }

    @Test
    void shouldPenalizeHighOpenIssueCount() {
        double scoreWith5Issues = metric.calculateScore(80, 5);
        double scoreWith60Issues = metric.calculateScore(80, 60);
        double scoreWith110Issues = metric.calculateScore(80, 110);

        assertThat(scoreWith60Issues).isLessThan(scoreWith5Issues);
        assertThat(scoreWith110Issues).isLessThan(scoreWith60Issues);
    }

    @Test
    void shouldHaveCorrectWeight() {
        assertThat(metric.getWeight()).isEqualTo(0.20);
    }
}
