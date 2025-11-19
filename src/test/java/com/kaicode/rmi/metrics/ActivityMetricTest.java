package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.MetricResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityMetricTest {

    @Mock
    private GitHubClient client;

    private ActivityMetric metric;

    @BeforeEach
    void setUp() {
        metric = new ActivityMetric();
    }

    @Test
    void shouldReturnHighScoreForRecentActivityHours() throws IOException {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<CommitInfo> commits = List.of(
                CommitInfo.builder()
                        .sha("abc123def")
                        .author("TestAuthor")
                        .message("Recent commit message that should be shortened")
                        .date(oneHourAgo)
                        .build()
        );

        when(client.getRecentCommits("owner", "repo", 10)).thenReturn(commits);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isGreaterThanOrEqualTo(90.0);
        assertThat(result.getName()).isEqualTo("Activity");
        assertThat(result.getDetails()).contains("TestAuthor").contains("hour ago").contains("Recent commit message").contains("abc123d");
    }

    @Test
    void shouldReturnLowScoreForOldActivity() throws IOException {
        LocalDateTime twoYearsAgo = LocalDateTime.now().minusYears(2);
        List<CommitInfo> commits = List.of(
                CommitInfo.builder()
                        .sha("abc123")
                        .message("Old commit")
                        .date(twoYearsAgo)
                        .build()
        );

        when(client.getRecentCommits("owner", "repo", 10)).thenReturn(commits);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isLessThanOrEqualTo(20.0);
    }

    @Test
    void shouldReturnZeroScoreForNoCommits() throws IOException {
        when(client.getRecentCommits("owner", "repo", 10)).thenReturn(List.of());

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isEqualTo(0.0);
    }

    @Test
    void shouldCalculateScoreCorrectlyForVariousDays() {
        assertThat(metric.calculateScoreFromDays(5)).isEqualTo(100.0);
        assertThat(metric.calculateScoreFromDays(15)).isEqualTo(90.0);
        assertThat(metric.calculateScoreFromDays(60)).isEqualTo(70.0);
        assertThat(metric.calculateScoreFromDays(120)).isEqualTo(50.0);
        assertThat(metric.calculateScoreFromDays(200)).isEqualTo(30.0);
        assertThat(metric.calculateScoreFromDays(400)).isEqualTo(10.0);
    }

    @Test
    void shouldHaveCorrectWeight() {
        assertThat(metric.getWeight()).isEqualTo(0.15);
    }
}
