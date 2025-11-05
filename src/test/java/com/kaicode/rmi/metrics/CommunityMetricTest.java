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
class CommunityMetricTest {

    @Mock
    private GitHubClient client;

    private CommunityMetric metric;

    @BeforeEach
    void setUp() {
        metric = new CommunityMetric();
    }

    @Test
    void shouldReturnHighScoreForPopularRepository() throws IOException {
        RepositoryInfo repo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .stars(1000)
                .forks(200)
                .build();

        when(client.getRepository("owner", "repo")).thenReturn(repo);
        when(client.getContributorCount("owner", "repo")).thenReturn(50);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isGreaterThan(80.0);
        assertThat(result.getName()).isEqualTo("Community");
    }

    @Test
    void shouldReturnLowScoreForUnpopularRepository() throws IOException {
        RepositoryInfo repo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .stars(0)
                .forks(0)
                .build();

        when(client.getRepository("owner", "repo")).thenReturn(repo);
        when(client.getContributorCount("owner", "repo")).thenReturn(1);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isLessThan(20.0);
    }

    @Test
    void shouldCalculateScoreCorrectly() {
        double score1 = metric.calculateScore(100, 20, 5);
        double score2 = metric.calculateScore(500, 100, 10);
        double score3 = metric.calculateScore(1000, 200, 20);

        assertThat(score2).isGreaterThan(score1);
        assertThat(score3).isGreaterThan(score2);
    }

    @Test
    void shouldCapScoreAt100() {
        double score = metric.calculateScore(10000, 5000, 100);
        assertThat(score).isLessThanOrEqualTo(100.0);
    }

    @Test
    void shouldHaveCorrectWeight() {
        assertThat(metric.getWeight()).isEqualTo(0.15);
    }
}
