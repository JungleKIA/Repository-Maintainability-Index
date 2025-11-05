package com.kaicode.rmi.util;

import com.kaicode.rmi.model.LLMAnalysis;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LLMReportFormatterTest {

    private LLMReportFormatter formatter;
    private MaintainabilityReport report;
    private LLMAnalysis llmAnalysis;

    @BeforeEach
    void setUp() {
        formatter = new LLMReportFormatter();

        Map<String, MetricResult> metrics = new HashMap<>();
        metrics.put("Test", MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(0.5)
                .description("Test metric")
                .build());

        report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(60)
                .metrics(metrics)
                .recommendation("Test recommendation")
                .build();

        llmAnalysis = LLMAnalysis.builder()
                .readmeAnalysis(new LLMAnalysis.ReadmeAnalysis(
                        7, 5, 6,
                        List.of("Strength 1"),
                        List.of("Suggestion 1")
                ))
                .commitAnalysis(new LLMAnalysis.CommitAnalysis(
                        8, 6, 7,
                        List.of("Pattern 1")
                ))
                .communityAnalysis(new LLMAnalysis.CommunityAnalysis(
                        3, 3, 4,
                        List.of("Strength 1"),
                        List.of("Suggestion 1")
                ))
                .recommendations(List.of(
                        new LLMAnalysis.AIRecommendation(
                                "Test Recommendation",
                                "Test description",
                                80, 85, "🔴"
                        )
                ))
                .confidence(75.0)
                .tokensUsed(1000)
                .build();
    }

    @Test
    void shouldFormatWithLLM() {
        String output = formatter.formatWithLLM(report, llmAnalysis);

        assertThat(output).contains("🤖 LLM INSIGHTS");
        assertThat(output).contains("📖 README Analysis");
        assertThat(output).contains("📝 Commit Quality");
        assertThat(output).contains("👥 Community Health");
        assertThat(output).contains("💡 TOP AI RECOMMENDATIONS");
        assertThat(output).contains("📊 API LIMITS STATUS");
        assertThat(output).contains("💡 RECOMMENDATIONS");
    }

    @Test
    void shouldIncludeReadmeScores() {
        String output = formatter.formatWithLLM(report, llmAnalysis);

        assertThat(output).contains("Clarity: 7/10");
        assertThat(output).contains("Completeness: 5/10");
        assertThat(output).contains("Newcomer Friendly: 6/10");
    }

    @Test
    void shouldIncludeCommitScores() {
        String output = formatter.formatWithLLM(report, llmAnalysis);

        assertThat(output).contains("Clarity: 8/10");
        assertThat(output).contains("Consistency: 6/10");
        assertThat(output).contains("Informativeness: 7/10");
    }

    @Test
    void shouldIncludeCommunityScores() {
        String output = formatter.formatWithLLM(report, llmAnalysis);

        assertThat(output).contains("Responsiveness: 3/10");
        assertThat(output).contains("Helpfulness: 3/10");
        assertThat(output).contains("Tone: 4/10");
    }

    @Test
    void shouldIncludeRecommendations() {
        String output = formatter.formatWithLLM(report, llmAnalysis);

        assertThat(output).contains("Test Recommendation");
        assertThat(output).contains("Impact: 80%");
        assertThat(output).contains("Confidence: 85%");
    }

    @Test
    void shouldIncludeConfidenceAndTokens() {
        String output = formatter.formatWithLLM(report, llmAnalysis);

        assertThat(output).contains("75.0% confidence");
        assertThat(output).contains("1000 tokens used");
    }

    @Test
    void shouldIncludeEmojis() {
        String output = formatter.formatWithLLM(report, llmAnalysis);

        assertThat(output).contains("🟡");
        assertThat(output).contains("🟠");
        assertThat(output).contains("🔴");
        assertThat(output).contains("🟢");
        assertThat(output).contains("🥇");
    }

    @Test
    void shouldHandleMultipleRecommendations() {
        LLMAnalysis multiRecAnalysis = LLMAnalysis.builder()
                .readmeAnalysis(llmAnalysis.getReadmeAnalysis())
                .commitAnalysis(llmAnalysis.getCommitAnalysis())
                .communityAnalysis(llmAnalysis.getCommunityAnalysis())
                .recommendations(List.of(
                        new LLMAnalysis.AIRecommendation("Rec 1", "Desc 1", 90, 90, "🔴"),
                        new LLMAnalysis.AIRecommendation("Rec 2", "Desc 2", 80, 80, "🔴"),
                        new LLMAnalysis.AIRecommendation("Rec 3", "Desc 3", 70, 70, "🟡"),
                        new LLMAnalysis.AIRecommendation("Rec 4", "Desc 4", 60, 60, "🟡")
                ))
                .confidence(75.0)
                .tokensUsed(1000)
                .build();

        String output = formatter.formatWithLLM(report, multiRecAnalysis);

        assertThat(output).contains("... and 1 more recommendations");
    }
}
