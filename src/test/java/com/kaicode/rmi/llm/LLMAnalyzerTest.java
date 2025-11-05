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
}
