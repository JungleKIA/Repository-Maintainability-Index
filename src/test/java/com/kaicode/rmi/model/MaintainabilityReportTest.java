package com.kaicode.rmi.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaintainabilityReportTest {

    @Test
    void shouldBuildMaintainabilityReportWithAllFields() {
        Map<String, MetricResult> metrics = new HashMap<>();
        metrics.put("Test", MetricResult.builder()
                .name("Test")
                .score(80)
                .weight(0.5)
                .build());

        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(85.5)
                .metrics(metrics)
                .recommendation("Good job")
                .build();

        assertThat(report.getRepositoryFullName()).isEqualTo("owner/repo");
        assertThat(report.getOverallScore()).isEqualTo(85.5);
        assertThat(report.getMetrics()).hasSize(1);
        assertThat(report.getRecommendation()).isEqualTo("Good job");
    }

    @ParameterizedTest
    @CsvSource({
            "95, EXCELLENT",
            "90, EXCELLENT",
            "85, GOOD",
            "75, GOOD",
            "70, FAIR",
            "60, FAIR",
            "50, POOR",
            "40, POOR",
            "30, CRITICAL",
            "10, CRITICAL"
    })
    void shouldReturnCorrectRating(double score, String expectedRating) {
        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(score)
                .metrics(new HashMap<>())
                .build();

        assertThat(report.getRating()).isEqualTo(expectedRating);
    }

    @Test
    void shouldReturnImmutableMetricsMap() {
        Map<String, MetricResult> metrics = new HashMap<>();
        metrics.put("Test", MetricResult.builder()
                .name("Test")
                .score(80)
                .weight(0.5)
                .build());

        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(80)
                .metrics(metrics)
                .build();

        assertThatThrownBy(() -> report.getMetrics().put("New", null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionWhenRepositoryNameIsNull() {
        assertThatThrownBy(() -> MaintainabilityReport.builder()
                .overallScore(80)
                .metrics(new HashMap<>())
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenMetricsIsNull() {
        assertThatThrownBy(() -> MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(80)
                .build())
                .isInstanceOf(NullPointerException.class);
    }
}
