package com.kaicode.rmi.util;

import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportFormatterEdgeCaseTest {

    @Test
    void shouldFormatReportWithEmptyRecommendation() {
        ReportFormatter formatter = new ReportFormatter();
        
        Map<String, MetricResult> metrics = new HashMap<>();
        metrics.put("Test", MetricResult.builder()
                .name("Test")
                .score(80)
                .weight(0.5)
                .description("Test")
                .build());
        
        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(80)
                .metrics(metrics)
                .recommendation(null)
                .build();
        
        String textOutput = formatter.format(report, ReportFormatter.OutputFormat.TEXT);
        assertThat(textOutput).isNotNull();
        
        String jsonOutput = formatter.format(report, ReportFormatter.OutputFormat.JSON);
        assertThat(jsonOutput).contains("\"recommendation\": \"\"");
    }

    @Test
    void shouldFormatReportWithEmptyMetrics() {
        ReportFormatter formatter = new ReportFormatter();
        
        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(0)
                .metrics(new LinkedHashMap<>())
                .recommendation("No metrics")
                .build();
        
        String textOutput = formatter.format(report, ReportFormatter.OutputFormat.TEXT);
        assertThat(textOutput).contains("owner/repo");
        assertThat(textOutput).contains("0.00");
        
        String jsonOutput = formatter.format(report, ReportFormatter.OutputFormat.JSON);
        assertThat(jsonOutput).contains("\"metrics\": {");
        assertThat(jsonOutput).contains("}");
    }

    @Test
    void shouldEscapeAllSpecialCharactersInJson() {
        ReportFormatter formatter = new ReportFormatter();
        
        Map<String, MetricResult> metrics = new LinkedHashMap<>();
        metrics.put("Test", MetricResult.builder()
                .name("Test")
                .score(80)
                .weight(0.5)
                .description("Test with\ttab\rand\rcarriage\nreturn")
                .details("Path: C:\\Users\\test")
                .build());
        
        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(80)
                .metrics(metrics)
                .recommendation("Test\"quote\"")
                .build();
        
        String jsonOutput = formatter.format(report, ReportFormatter.OutputFormat.JSON);
        
        assertThat(jsonOutput).contains("\\t");
        assertThat(jsonOutput).contains("\\r");
        assertThat(jsonOutput).contains("\\n");
        assertThat(jsonOutput).contains("\\\\");
        assertThat(jsonOutput).contains("\\\"");
    }

    @Test
    void shouldFormatTextWithLongDetails() {
        ReportFormatter formatter = new ReportFormatter();
        
        Map<String, MetricResult> metrics = new LinkedHashMap<>();
        metrics.put("Test", MetricResult.builder()
                .name("Test")
                .score(80)
                .weight(0.5)
                .description("Short description")
                .details("This is a very long details string that contains a lot of information " +
                        "about the metric and its calculation process and results")
                .build());
        
        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(80)
                .metrics(metrics)
                .recommendation("Good")
                .build();
        
        String textOutput = formatter.format(report, ReportFormatter.OutputFormat.TEXT);
        assertThat(textOutput).contains("This is a very long details string");
    }

    @Test
    void shouldFormatJsonWithMultipleMetrics() {
        ReportFormatter formatter = new ReportFormatter();
        
        Map<String, MetricResult> metrics = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            metrics.put("Metric" + i, MetricResult.builder()
                    .name("Metric" + i)
                    .score(80 + i)
                    .weight(0.2)
                    .description("Description " + i)
                    .details("Details " + i)
                    .build());
        }
        
        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(82)
                .metrics(metrics)
                .recommendation("Good")
                .build();
        
        String jsonOutput = formatter.format(report, ReportFormatter.OutputFormat.JSON);
        
        assertThat(jsonOutput).contains("\"Metric1\"");
        assertThat(jsonOutput).contains("\"Metric2\"");
        assertThat(jsonOutput).contains("\"Metric3\"");
        assertThat(jsonOutput).contains("\"Metric4\"");
        assertThat(jsonOutput).contains("\"Metric5\"");
    }

    @Test
    void shouldFormatDifferentRatings() {
        ReportFormatter formatter = new ReportFormatter();
        
        Map<String, MetricResult> metrics = new HashMap<>();
        
        for (double score : new double[]{95, 85, 70, 50, 30}) {
            MaintainabilityReport report = MaintainabilityReport.builder()
                    .repositoryFullName("owner/repo")
                    .overallScore(score)
                    .metrics(metrics)
                    .build();
            
            String textOutput = formatter.format(report, ReportFormatter.OutputFormat.TEXT);
            assertThat(textOutput).contains(String.format("%.2f", score));
        }
    }
}
