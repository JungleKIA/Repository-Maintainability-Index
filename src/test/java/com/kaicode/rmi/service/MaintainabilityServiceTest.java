package com.kaicode.rmi.service;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.metrics.MetricCalculator;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintainabilityServiceTest {

    @Mock
    private GitHubClient client;

    @Mock
    private MetricCalculator metric1;

    @Mock
    private MetricCalculator metric2;

    private MaintainabilityService service;

    @BeforeEach
    void setUp() {
        List<MetricCalculator> calculators = List.of(metric1, metric2);
        service = new MaintainabilityService(client, calculators);
    }

    @Test
    void shouldAnalyzeRepositorySuccessfully() throws IOException {
        MetricResult result1 = MetricResult.builder()
                .name("Metric1")
                .score(80)
                .weight(0.5)
                .description("Test metric 1")
                .build();

        MetricResult result2 = MetricResult.builder()
                .name("Metric2")
                .score(90)
                .weight(0.5)
                .description("Test metric 2")
                .build();

        when(metric1.calculate(eq(client), eq("owner"), eq("repo"))).thenReturn(result1);
        when(metric2.calculate(eq(client), eq("owner"), eq("repo"))).thenReturn(result2);

        MaintainabilityReport report = service.analyze("owner", "repo");

        assertThat(report.getRepositoryFullName()).isEqualTo("owner/repo");
        assertThat(report.getOverallScore()).isEqualTo(85.0);
        assertThat(report.getMetrics()).hasSize(2);
        assertThat(report.getRating()).isEqualTo("GOOD");
    }

    @Test
    void shouldCalculateWeightedAverageCorrectly() throws IOException {
        MetricResult result1 = MetricResult.builder()
                .name("Metric1")
                .score(100)
                .weight(0.3)
                .description("Test")
                .build();

        MetricResult result2 = MetricResult.builder()
                .name("Metric2")
                .score(50)
                .weight(0.7)
                .description("Test")
                .build();

        when(metric1.calculate(any(), any(), any())).thenReturn(result1);
        when(metric2.calculate(any(), any(), any())).thenReturn(result2);

        MaintainabilityReport report = service.analyze("owner", "repo");

        double expectedScore = (100 * 0.3 + 50 * 0.7) / 1.0;
        assertThat(report.getOverallScore()).isEqualTo(expectedScore);
    }

    @Test
    void shouldThrowExceptionWhenMetricCalculationFails() throws IOException {
        when(metric1.calculate(any(), any(), any()))
                .thenThrow(new IOException("API error"));

        assertThatThrownBy(() -> service.analyze("owner", "repo"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void shouldGenerateRecommendationForExcellentScore() {
        Map<String, MetricResult> metrics = Map.of();
        String recommendation = service.generateRecommendation(95, metrics);

        assertThat(recommendation).contains("Excellent");
        assertThat(recommendation).contains("Keep up the good work");
    }

    @Test
    void shouldGenerateRecommendationWithImprovements() {
        MetricResult poorMetric = MetricResult.builder()
                .name("PoorMetric")
                .score(40)
                .weight(0.5)
                .description("Test")
                .build();

        Map<String, MetricResult> metrics = Map.of("PoorMetric", poorMetric);
        String recommendation = service.generateRecommendation(50, metrics);

        assertThat(recommendation).contains("improvement");
        assertThat(recommendation).contains("PoorMetric");
    }

    @Test
    void shouldGenerateRecommendationForFairScore() {
        Map<String, MetricResult> metrics = Map.of();
        String recommendation = service.generateRecommendation(65, metrics);

        assertThat(recommendation).contains("Fair");
    }

    @Test
    void shouldGenerateRecommendationForGoodScore() {
        Map<String, MetricResult> metrics = Map.of();
        String recommendation = service.generateRecommendation(80, metrics);

        assertThat(recommendation).contains("Good");
    }

    @Test
    void shouldThrowExceptionWhenClientIsNull() {
        assertThatThrownBy(() -> new MaintainabilityService(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenCalculatorsAreNull() {
        assertThatThrownBy(() -> new MaintainabilityService(client, null))
                .isInstanceOf(NullPointerException.class);
    }
}
