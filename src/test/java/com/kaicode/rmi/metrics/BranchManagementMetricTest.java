package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchManagementMetricTest {

    @Mock
    private GitHubClient client;

    private BranchManagementMetric metric;

    @BeforeEach
    void setUp() {
        metric = new BranchManagementMetric();
    }

    @Test
    void shouldReturnHighScoreForFewBranches() throws IOException {
        when(client.getBranchCount("owner", "repo")).thenReturn(2);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isEqualTo(100.0);
        assertThat(result.getName()).isEqualTo("Branch Management");
    }

    @Test
    void shouldReturnLowScoreForManyBranches() throws IOException {
        when(client.getBranchCount("owner", "repo")).thenReturn(100);

        MetricResult result = metric.calculate(client, "owner", "repo");

        assertThat(result.getScore()).isLessThan(50.0);
    }

    @Test
    void shouldCalculateScoreCorrectly() {
        assertThat(metric.calculateScore(2)).isEqualTo(100.0);
        assertThat(metric.calculateScore(5)).isEqualTo(95.0);
        assertThat(metric.calculateScore(8)).isEqualTo(85.0);
        assertThat(metric.calculateScore(15)).isEqualTo(70.0);
        assertThat(metric.calculateScore(30)).isEqualTo(50.0);
        assertThat(metric.calculateScore(60)).isEqualTo(30.0);
    }

    @Test
    void shouldHaveCorrectWeight() {
        assertThat(metric.getWeight()).isEqualTo(0.15);
    }
}
