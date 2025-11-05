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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMAnalyzerEdgeCaseTest {

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
    void shouldHandleManyCommits() throws IOException {
        List<CommitInfo> commits = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            commits.add(CommitInfo.builder()
                    .sha("sha" + i)
                    .message("Message " + i)
                    .date(LocalDateTime.now())
                    .build());
        }

        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(commits);
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        assertThat(analysis.getCommitAnalysis()).isNotNull();
    }

    @Test
    void shouldGenerateAllRecommendations() throws IOException {
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        assertThat(analysis.getRecommendations()).hasSizeGreaterThan(3);
        
        boolean hasHighImpact = analysis.getRecommendations().stream()
                .anyMatch(r -> r.getImpact() >= 70);
        assertThat(hasHighImpact).isTrue();
    }

    @Test
    void shouldSortRecommendationsByImpact() throws IOException {
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        List<LLMAnalysis.AIRecommendation> recs = analysis.getRecommendations();
        for (int i = 0; i < recs.size() - 1; i++) {
            assertThat(recs.get(i).getImpact())
                    .isGreaterThanOrEqualTo(recs.get(i + 1).getImpact());
        }
    }

    @Test
    void shouldCalculateConfidenceBasedOnScores() throws IOException {
        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        assertThat(analysis.getConfidence()).isBetween(25.0, 95.0);
    }

    @Test
    void shouldHandleVeryLongCommitMessage() throws IOException {
        String longMessage = "a".repeat(5000);
        List<CommitInfo> commits = List.of(
                CommitInfo.builder()
                        .sha("abc")
                        .message(longMessage)
                        .date(LocalDateTime.now())
                        .build()
        );

        when(githubClient.getRecentCommits(anyString(), anyString(), anyInt()))
                .thenReturn(commits);
        when(llmClient.analyze(anyString()))
                .thenThrow(new IOException("API error"));

        LLMAnalysis analysis = analyzer.analyze(githubClient, "owner", "repo");

        assertThat(analysis.getCommitAnalysis()).isNotNull();
    }
}
