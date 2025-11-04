package com.kaicode.rmi.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricResultTest {

    @Test
    void shouldBuildMetricResultWithAllFields() {
        MetricResult result = MetricResult.builder()
                .name("Test Metric")
                .score(85.5)
                .weight(0.25)
                .description("Test description")
                .details("Test details")
                .build();

        assertThat(result.getName()).isEqualTo("Test Metric");
        assertThat(result.getScore()).isEqualTo(85.5);
        assertThat(result.getWeight()).isEqualTo(0.25);
        assertThat(result.getDescription()).isEqualTo("Test description");
        assertThat(result.getDetails()).isEqualTo("Test details");
    }

    @Test
    void shouldCalculateWeightedScore() {
        MetricResult result = MetricResult.builder()
                .name("Test")
                .score(80.0)
                .weight(0.5)
                .build();

        assertThat(result.getWeightedScore()).isEqualTo(40.0);
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> MetricResult.builder()
                .score(50)
                .weight(0.5)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 101, 150})
    void shouldThrowExceptionWhenScoreIsOutOfRange(double score) {
        assertThatThrownBy(() -> MetricResult.builder()
                .name("Test")
                .score(score)
                .weight(0.5)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("score");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1, 2.0})
    void shouldThrowExceptionWhenWeightIsOutOfRange(double weight) {
        assertThatThrownBy(() -> MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(weight)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weight");
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        MetricResult result1 = MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(0.5)
                .build();

        MetricResult result2 = MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(0.5)
                .description("Different")
                .build();

        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }
}
