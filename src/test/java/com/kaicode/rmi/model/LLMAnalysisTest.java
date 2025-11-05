package com.kaicode.rmi.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LLMAnalysisTest {

    @Test
    void shouldBuildLLMAnalysis() {
        LLMAnalysis.ReadmeAnalysis readme = new LLMAnalysis.ReadmeAnalysis(
                7, 5, 6,
                List.of("Strength"),
                List.of("Suggestion")
        );

        LLMAnalysis.CommitAnalysis commit = new LLMAnalysis.CommitAnalysis(
                8, 6, 7,
                List.of("Pattern")
        );

        LLMAnalysis.CommunityAnalysis community = new LLMAnalysis.CommunityAnalysis(
                3, 3, 4,
                List.of("Strength"),
                List.of("Suggestion")
        );

        LLMAnalysis.AIRecommendation recommendation = new LLMAnalysis.AIRecommendation(
                "Title", "Description", 80, 85, "🔴"
        );

        LLMAnalysis analysis = LLMAnalysis.builder()
                .readmeAnalysis(readme)
                .commitAnalysis(commit)
                .communityAnalysis(community)
                .recommendations(List.of(recommendation))
                .confidence(75.0)
                .tokensUsed(1000)
                .build();

        assertThat(analysis.getReadmeAnalysis()).isEqualTo(readme);
        assertThat(analysis.getCommitAnalysis()).isEqualTo(commit);
        assertThat(analysis.getCommunityAnalysis()).isEqualTo(community);
        assertThat(analysis.getRecommendations()).hasSize(1);
        assertThat(analysis.getConfidence()).isEqualTo(75.0);
        assertThat(analysis.getTokensUsed()).isEqualTo(1000);
    }

    @Test
    void shouldGetReadmeScores() {
        LLMAnalysis.ReadmeAnalysis readme = new LLMAnalysis.ReadmeAnalysis(
                7, 5, 6,
                List.of("Strength"),
                List.of("Suggestion")
        );

        assertThat(readme.getClarity()).isEqualTo(7);
        assertThat(readme.getCompleteness()).isEqualTo(5);
        assertThat(readme.getNewcomerFriendly()).isEqualTo(6);
        assertThat(readme.getStrengths()).hasSize(1);
        assertThat(readme.getSuggestions()).hasSize(1);
    }

    @Test
    void shouldGetCommitScores() {
        LLMAnalysis.CommitAnalysis commit = new LLMAnalysis.CommitAnalysis(
                8, 6, 7,
                List.of("Pattern1", "Pattern2")
        );

        assertThat(commit.getClarity()).isEqualTo(8);
        assertThat(commit.getConsistency()).isEqualTo(6);
        assertThat(commit.getInformativeness()).isEqualTo(7);
        assertThat(commit.getPatterns()).hasSize(2);
    }

    @Test
    void shouldGetCommunityScores() {
        LLMAnalysis.CommunityAnalysis community = new LLMAnalysis.CommunityAnalysis(
                3, 3, 4,
                List.of("Strength1", "Strength2"),
                List.of("Suggestion1")
        );

        assertThat(community.getResponsiveness()).isEqualTo(3);
        assertThat(community.getHelpfulness()).isEqualTo(3);
        assertThat(community.getTone()).isEqualTo(4);
        assertThat(community.getStrengths()).hasSize(2);
        assertThat(community.getSuggestions()).hasSize(1);
    }

    @Test
    void shouldGetRecommendationDetails() {
        LLMAnalysis.AIRecommendation rec = new LLMAnalysis.AIRecommendation(
                "Test Title",
                "Test Description",
                80, 85, "🔴"
        );

        assertThat(rec.getTitle()).isEqualTo("Test Title");
        assertThat(rec.getDescription()).isEqualTo("Test Description");
        assertThat(rec.getImpact()).isEqualTo(80);
        assertThat(rec.getConfidence()).isEqualTo(85);
        assertThat(rec.getSeverity()).isEqualTo("🔴");
    }

    @Test
    void shouldReturnImmutableCollections() {
        LLMAnalysis.ReadmeAnalysis readme = new LLMAnalysis.ReadmeAnalysis(
                7, 5, 6,
                List.of("Strength"),
                List.of("Suggestion")
        );

        List<String> strengths = readme.getStrengths();
        assertThat(strengths).hasSize(1);

        List<String> suggestions = readme.getSuggestions();
        assertThat(suggestions).hasSize(1);
    }
}
