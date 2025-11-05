package com.kaicode.rmi.util;

import com.kaicode.rmi.model.LLMAnalysis;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LLMReportFormatterEdgeCaseTest {

    private LLMReportFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new LLMReportFormatter();
    }

    @Test
    void shouldHandleEmptyStrengths() {
        MaintainabilityReport report = createBasicReport(50);
        
        LLMAnalysis analysis = LLMAnalysis.builder()
                .readmeAnalysis(new LLMAnalysis.ReadmeAnalysis(
                        5, 5, 5,
                        List.of(),
                        List.of("Suggestion")
                ))
                .commitAnalysis(new LLMAnalysis.CommitAnalysis(5, 5, 5, List.of()))
                .communityAnalysis(new LLMAnalysis.CommunityAnalysis(
                        5, 5, 5,
                        List.of(),
                        List.of()
                ))
                .recommendations(List.of())
                .confidence(50.0)
                .tokensUsed(500)
                .build();

        String output = formatter.formatWithLLM(report, analysis);

        assertThat(output).isNotEmpty();
    }

    @Test
    void shouldHandleManySuggestions() {
        List<String> manySuggestions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            manySuggestions.add("Suggestion " + i);
        }

        MaintainabilityReport report = createBasicReport(50);
        
        LLMAnalysis analysis = LLMAnalysis.builder()
                .readmeAnalysis(new LLMAnalysis.ReadmeAnalysis(
                        5, 5, 5,
                        List.of("Strength"),
                        manySuggestions
                ))
                .commitAnalysis(new LLMAnalysis.CommitAnalysis(5, 5, 5, manySuggestions.subList(0, 5)))
                .communityAnalysis(new LLMAnalysis.CommunityAnalysis(
                        5, 5, 5,
                        List.of("Strength"),
                        manySuggestions
                ))
                .recommendations(List.of())
                .confidence(50.0)
                .tokensUsed(500)
                .build();

        String output = formatter.formatWithLLM(report, analysis);

        assertThat(output).contains("+");
    }

    @Test
    void shouldHandleHighScores() {
        MaintainabilityReport report = createBasicReport(95);
        
        LLMAnalysis analysis = LLMAnalysis.builder()
                .readmeAnalysis(new LLMAnalysis.ReadmeAnalysis(
                        10, 10, 10,
                        List.of("Perfect!"),
                        List.of()
                ))
                .commitAnalysis(new LLMAnalysis.CommitAnalysis(10, 10, 10, List.of()))
                .communityAnalysis(new LLMAnalysis.CommunityAnalysis(
                        10, 10, 10,
                        List.of("Excellent!"),
                        List.of()
                ))
                .recommendations(List.of())
                .confidence(95.0)
                .tokensUsed(1000)
                .build();

        String output = formatter.formatWithLLM(report, analysis);

        assertThat(output).contains("🟢");
    }

    @Test
    void shouldHandleLowScores() {
        MaintainabilityReport report = createBasicReport(20);
        
        LLMAnalysis analysis = LLMAnalysis.builder()
                .readmeAnalysis(new LLMAnalysis.ReadmeAnalysis(
                        2, 2, 2,
                        List.of(),
                        List.of("Need improvement")
                ))
                .commitAnalysis(new LLMAnalysis.CommitAnalysis(2, 2, 2, List.of()))
                .communityAnalysis(new LLMAnalysis.CommunityAnalysis(
                        1, 1, 1,
                        List.of(),
                        List.of("Critical issues")
                ))
                .recommendations(List.of(
                        new LLMAnalysis.AIRecommendation("Fix 1", "Desc", 95, 90, "🔴"),
                        new LLMAnalysis.AIRecommendation("Fix 2", "Desc", 90, 85, "🔴"),
                        new LLMAnalysis.AIRecommendation("Fix 3", "Desc", 85, 80, "🔴")
                ))
                .confidence(30.0)
                .tokensUsed(500)
                .build();

        String output = formatter.formatWithLLM(report, analysis);

        assertThat(output).contains("🔴");
    }

    @Test
    void shouldHandleMediumScores() {
        MaintainabilityReport report = createBasicReport(60);
        
        LLMAnalysis analysis = LLMAnalysis.builder()
                .readmeAnalysis(new LLMAnalysis.ReadmeAnalysis(
                        6, 6, 6,
                        List.of("Okay"),
                        List.of("Can improve")
                ))
                .commitAnalysis(new LLMAnalysis.CommitAnalysis(6, 6, 6, List.of()))
                .communityAnalysis(new LLMAnalysis.CommunityAnalysis(
                        5, 5, 5,
                        List.of("Average"),
                        List.of("Room for growth")
                ))
                .recommendations(List.of())
                .confidence(60.0)
                .tokensUsed(750)
                .build();

        String output = formatter.formatWithLLM(report, analysis);

        assertThat(output).containsAnyOf("🟡", "🟠");
    }

    @Test
    void shouldIncludePoorMetricsInRecommendations() {
        Map<String, MetricResult> metrics = new HashMap<>();
        metrics.put("Poor", MetricResult.builder()
                .name("Poor Metric")
                .score(40)
                .weight(0.5)
                .description("Test")
                .build());

        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(45)
                .metrics(metrics)
                .build();
        
        LLMAnalysis analysis = LLMAnalysis.builder()
                .readmeAnalysis(new LLMAnalysis.ReadmeAnalysis(5, 5, 5, List.of(), List.of()))
                .commitAnalysis(new LLMAnalysis.CommitAnalysis(5, 5, 5, List.of()))
                .communityAnalysis(new LLMAnalysis.CommunityAnalysis(5, 5, 5, List.of(), List.of()))
                .recommendations(List.of())
                .confidence(50.0)
                .tokensUsed(500)
                .build();

        String output = formatter.formatWithLLM(report, analysis);

        assertThat(output).contains("Poor Metric");
    }

    private MaintainabilityReport createBasicReport(double score) {
        Map<String, MetricResult> metrics = new HashMap<>();
        metrics.put("Test", MetricResult.builder()
                .name("Test")
                .score(score)
                .weight(0.5)
                .description("Test")
                .build());

        return MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(score)
                .metrics(metrics)
                .build();
    }
}
