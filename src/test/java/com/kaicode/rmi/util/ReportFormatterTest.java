package com.kaicode.rmi.util;

import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportFormatterTest {

    private ReportFormatter formatter;
    private MaintainabilityReport report;

    @BeforeEach
    void setUp() {
        formatter = new ReportFormatter();

        Map<String, MetricResult> metrics = new LinkedHashMap<>();
        metrics.put("Documentation", MetricResult.builder()
                .name("Documentation")
                .score(85.5)
                .weight(0.20)
                .description("Test description")
                .details("Test details")
                .build());

        report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(87.3)
                .metrics(metrics)
                .recommendation("Keep up the good work!")
                .build();
    }

    @Test
    void shouldFormatAsText() {
        String output = formatter.format(report, ReportFormatter.OutputFormat.TEXT);

        assertThat(output).contains("Repository Maintainability Index Report");
        assertThat(output).contains("owner/repo");
        assertThat(output).contains("87.3");
        assertThat(output).contains("GOOD");
        assertThat(output).contains("Documentation");
        assertThat(output).contains("85.5");
        assertThat(output).contains("Keep up the good work!");
    }

    @Test
    void shouldFormatAsJson() {
        String output = formatter.format(report, ReportFormatter.OutputFormat.JSON);

        assertThat(output).contains("\"repository\"");
        assertThat(output).contains("\"owner/repo\"");
        assertThat(output).contains("\"overallScore\"");
        assertThat(output).contains("87.3");
        assertThat(output).contains("\"rating\"");
        assertThat(output).contains("\"GOOD\"");
        assertThat(output).contains("\"Documentation\"");
        assertThat(output).contains("\"score\"");
        assertThat(output).contains("85.5");
        assertThat(output).contains("\"recommendation\"");
    }

    @Test
    void shouldEscapeSpecialCharactersInJson() {
        Map<String, MetricResult> metrics = new LinkedHashMap<>();
        metrics.put("Test", MetricResult.builder()
                .name("Test")
                .score(80)
                .weight(0.5)
                .description("Test with \"quotes\" and \n newline")
                .build());

        MaintainabilityReport reportWithSpecialChars = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(80)
                .metrics(metrics)
                .recommendation("Test\\path")
                .build();

        String output = formatter.format(reportWithSpecialChars, ReportFormatter.OutputFormat.JSON);

        assertThat(output).contains("\\\"quotes\\\"");
        assertThat(output).contains("\\n");
        assertThat(output).contains("\\\\path");
    }

    @Test
    void shouldHandleMultipleMetrics() {
        Map<String, MetricResult> metrics = new LinkedHashMap<>();
        metrics.put("Metric1", MetricResult.builder()
                .name("Metric1")
                .score(80)
                .weight(0.5)
                .description("First")
                .build());
        metrics.put("Metric2", MetricResult.builder()
                .name("Metric2")
                .score(90)
                .weight(0.5)
                .description("Second")
                .build());

        MaintainabilityReport multiMetricReport = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(85)
                .metrics(metrics)
                .recommendation("Good")
                .build();

        String textOutput = formatter.format(multiMetricReport, ReportFormatter.OutputFormat.TEXT);
        assertThat(textOutput).contains("Metric1");
        assertThat(textOutput).contains("Metric2");

        String jsonOutput = formatter.format(multiMetricReport, ReportFormatter.OutputFormat.JSON);
        assertThat(jsonOutput).contains("\"Metric1\"");
        assertThat(jsonOutput).contains("\"Metric2\"");
    }

    @Test
    void shouldHandleNullDetails() {
        Map<String, MetricResult> metrics = new LinkedHashMap<>();
        metrics.put("Test", MetricResult.builder()
                .name("Test")
                .score(80)
                .weight(0.5)
                .description("No details")
                .details(null)
                .build());

        MaintainabilityReport reportWithNullDetails = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(80)
                .metrics(metrics)
                .build();

        String textOutput = formatter.format(reportWithNullDetails, ReportFormatter.OutputFormat.TEXT);
        assertThat(textOutput).isNotNull();

        String jsonOutput = formatter.format(reportWithNullDetails, ReportFormatter.OutputFormat.JSON);
        assertThat(jsonOutput).isNotNull();
    }
}
