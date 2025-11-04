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
}
