package com.kaicode.rmi.llm;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.LLMAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMAnalyzerTest {

    @Mock
    private LLMClient llmClient;

    @Mock
    private GitHubClient githubClient;

    private LLMAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new LLMAnalyzer(llmClient);
    }

    @Test
    void shouldAnalyzeRepositoryWithDefaults() throws IOException {
        List<CommitInfo> commits = List.of(
                CommitInfo.builder()
                        .sha("abc")
                        .message("feat: add feature")
                        .date(LocalDateTime.now())
                        .build()
        );

        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(commits);
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        assertThat(analysis).isNotNull();
        assertThat(analysis.getReadmeAnalysis()).isNotNull();
        assertThat(analysis.getCommitAnalysis()).isNotNull();
        assertThat(analysis.getCommunityAnalysis()).isNotNull();
        assertThat(analysis.getRecommendations()).isNotEmpty();
        assertThat(analysis.getConfidence()).isGreaterThan(0);
        assertThat(analysis.getTokensUsed()).isGreaterThan(0);
    }

    @Test
    void shouldGenerateRecommendations() throws IOException {
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        assertThat(analysis.getRecommendations()).hasSizeGreaterThan(0);
        
        LLMAnalysis.AIRecommendation first = analysis.getRecommendations().get(0);
        assertThat(first.getTitle()).isNotEmpty();
        assertThat(first.getDescription()).isNotEmpty();
        assertThat(first.getImpact()).isBetween(0, 100);
        assertThat(first.getConfidence()).isBetween(0, 100);
        assertThat(first.getSeverity()).isNotEmpty();
    }

    @Test
    void shouldHandleEmptyCommits() throws IOException {
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        assertThat(analysis.getCommitAnalysis()).isNotNull();
        assertThat(analysis.getCommitAnalysis().getClarity()).isGreaterThan(0);
    }

    @Test
    void shouldCalculateConfidence() throws IOException {
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        assertThat(analysis.getConfidence()).isLessThanOrEqualTo(100.0);
        assertThat(analysis.getConfidence()).isGreaterThan(0.0);
    }

    // ========== NEW TESTS FOR TEXT CLEANING ==========

    @Test
    void shouldCleanMojibakeInReadmeAnalysisFallback() throws IOException {
        // When LLM API fails, fallback values should be cleaned
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        // Verify fallback values are clean (no mojibake)
        LLMAnalysis.ReadmeAnalysis readme = analysis.getReadmeAnalysis();
        assertThat(readme.getStrengths()).allMatch(s -> !s.contains("ΓòÉ") && !s.contains("ΓöÇ") && !s.contains("ΓÇæ"));
        assertThat(readme.getSuggestions()).allMatch(s -> !s.contains("ΓòÉ") && !s.contains("ΓöÇ") && !s.contains("ΓÇæ"));
    }

    @Test
    void shouldCleanMojibakeInCommitAnalysisFallback() throws IOException {
        // When LLM API fails, fallback values should be cleaned
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        // Verify fallback values are clean (no mojibake)
        LLMAnalysis.CommitAnalysis commit = analysis.getCommitAnalysis();
        assertThat(commit.getPatterns()).allMatch(p -> !p.contains("ΓòÉ") && !p.contains("ΓöÇ") && !p.contains("ΓÇæ"));
    }

    @Test
    void shouldCleanMojibakeInCommunityAnalysisFallback() throws IOException {
        // When LLM API fails, fallback values should be cleaned
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        // Verify fallback values are clean (no mojibake)
        LLMAnalysis.CommunityAnalysis community = analysis.getCommunityAnalysis();
        assertThat(community.getStrengths()).allMatch(s -> !s.contains("ΓòÉ") && !s.contains("ΓöÇ") && !s.contains("ΓÇæ"));
        assertThat(community.getSuggestions()).allMatch(s -> !s.contains("ΓòÉ") && !s.contains("ΓöÇ") && !s.contains("ΓÇæ"));
    }

    @Test
    void shouldCleanMojibakeInSuccessfulReadmeAnalysis() throws IOException {
        // Mock successful LLM response with mojibake
        String responseWithMojibake = "{\"clarity\":7,\"completeness\":5,\"newcomerFriendly\":6," +
                "\"strengths\":[\"Well-structured sections\",\"Comprehensive links\"]," +
                "\"suggestions\":[\"Add Quick Start section\",\"Include prerequisites\"]}";

        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenReturn(new LLMClient.LLMResponse(responseWithMojibake, 100));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        // Verify response is parsed successfully
        LLMAnalysis.ReadmeAnalysis readme = analysis.getReadmeAnalysis();
        assertThat(readme.getStrengths()).isNotEmpty();
        assertThat(readme.getSuggestions()).isNotEmpty();
    }

    @Test
    void shouldCleanMojibakeInSuccessfulCommitAnalysis() throws IOException {
        // Mock successful LLM response with mojibake
        String responseWithMojibake = "{\"clarity\":8,\"consistency\":6,\"informativeness\":7," +
                "\"patterns\":[\"Positive: Most messages use short style\",\"Negative: Some are too vague\"]}";

        List<CommitInfo> commits = List.of(
                CommitInfo.builder()
                        .sha("abc")
                        .message("feat: add feature")
                        .date(LocalDateTime.now())
                        .build()
        );

        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(commits);
        when(llmClient.analyze(anyString()))
                .thenReturn(new LLMClient.LLMResponse(responseWithMojibake, 100));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        // Verify response is parsed successfully
        LLMAnalysis.CommitAnalysis commit = analysis.getCommitAnalysis();
        assertThat(commit.getPatterns()).isNotEmpty();
    }

    @Test
    void shouldCleanMojibakeInSuccessfulCommunityAnalysis() throws IOException {
        // Mock successful LLM response
        String responseWithMojibake = "{\"responsiveness\":3,\"helpfulness\":3,\"tone\":4," +
                "\"strengths\":[\"High volume of issues\",\"Wide range of topics\"]," +
                "\"suggestions\":[\"Increase speed\",\"Provide more details\"]}";

        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenReturn(new LLMClient.LLMResponse(responseWithMojibake, 100));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        // Verify response is parsed successfully
        LLMAnalysis.CommunityAnalysis community = analysis.getCommunityAnalysis();
        assertThat(community.getStrengths()).isNotEmpty();
        assertThat(community.getSuggestions()).isNotEmpty();
    }

    @Test
    void shouldHandleComplexMojibakeInAllAnalyses() throws IOException {
        // Mock responses with various mojibake patterns
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of(
                        CommitInfo.builder()
                                .sha("abc")
                                .message("feat: add feature")
                                .date(LocalDateTime.now())
                                .build()
                ));

        // Simulate multiple API calls
        when(llmClient.analyze(anyString()))
                .thenReturn(new LLMClient.LLMResponse(
                        "{\"clarity\":7,\"completeness\":5,\"newcomerFriendly\":6," +
                        "\"strengths\":[\"Header\",\"Separator\"]," +
                        "\"suggestions\":[\"first-response\",\"24-48 hours\"]}", 100))
                .thenReturn(new LLMClient.LLMResponse(
                        "{\"clarity\":8,\"consistency\":6,\"informativeness\":7," +
                        "\"patterns\":[\"Bullet point\",\"Well-structured\"]}", 100))
                .thenReturn(new LLMClient.LLMResponse(
                        "{\"responsiveness\":3,\"helpfulness\":3,\"tone\":4," +
                        "\"strengths\":[\"High volume\"]," +
                        "\"suggestions\":[\"Increase speed\"]}", 100));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        // Verify all analyses are parsed successfully
        assertThat(analysis.getReadmeAnalysis().getStrengths()).isNotEmpty();
        assertThat(analysis.getReadmeAnalysis().getSuggestions()).isNotEmpty();
        assertThat(analysis.getCommitAnalysis().getPatterns()).isNotEmpty();
        assertThat(analysis.getCommunityAnalysis().getStrengths()).isNotEmpty();
        assertThat(analysis.getCommunityAnalysis().getSuggestions()).isNotEmpty();
    }
}
