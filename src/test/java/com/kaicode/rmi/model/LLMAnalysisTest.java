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

    // ========== NEW TESTS FOR TEXT CLEANING IN CONSTRUCTORS ==========

    @Test
    void shouldCleanMojibakeInReadmeAnalysisConstructor() {
        // Create ReadmeAnalysis with mojibake in strengths and suggestions
        LLMAnalysis.ReadmeAnalysis readme = new LLMAnalysis.ReadmeAnalysis(
                7, 5, 6,
                List.of("ΓòÉΓòÉ Header", "ΓöÇΓöÇ Separator"),
                List.of("firstΓÇæresponse", "24ΓÇô48 hours")
        );

        // Verify mojibake is cleaned
        assertThat(readme.getStrengths()).allMatch(s -> !s.contains("ΓòÉ") && !s.contains("ΓöÇ"));
        assertThat(readme.getSuggestions()).allMatch(s -> !s.contains("ΓÇæ") && !s.contains("ΓÇô"));
        
        // Verify cleaned values contain expected text
        assertThat(readme.getStrengths().get(0)).contains("══ Header");
        assertThat(readme.getStrengths().get(1)).contains("── Separator");
        assertThat(readme.getSuggestions()).contains("first-response", "24-48 hours");
    }

    @Test
    void shouldCleanMojibakeInCommitAnalysisConstructor() {
        // Create CommitAnalysis with mojibake in patterns
        LLMAnalysis.CommitAnalysis commit = new LLMAnalysis.CommitAnalysis(
                8, 6, 7,
                List.of("Γû¬ Bullet point", "Well-structured", "firstΓÇæresponse")
        );

        // Verify mojibake is cleaned
        assertThat(commit.getPatterns()).allMatch(p -> !p.contains("Γû¬") && !p.contains("ΓÇæ"));
        
        // Verify cleaned values
        assertThat(commit.getPatterns()).contains("▪ Bullet point", "first-response");
    }

    @Test
    void shouldCleanMojibakeInCommunityAnalysisConstructor() {
        // Create CommunityAnalysis with mojibake in strengths and suggestions
        LLMAnalysis.CommunityAnalysis community = new LLMAnalysis.CommunityAnalysis(
                3, 3, 4,
                List.of("High volume", "Wide rangeΓÇæ"),
                List.of("Increase speed", "Provide detailsΓÇô")
        );

        // Verify mojibake is cleaned
        assertThat(community.getStrengths()).allMatch(s -> !s.contains("ΓÇæ"));
        assertThat(community.getSuggestions()).allMatch(s -> !s.contains("ΓÇô"));
        
        // Verify cleaned values contain expected text
        assertThat(community.getStrengths().get(0)).contains("High volume");
        assertThat(community.getStrengths().get(1)).contains("Wide range");
    }

    @Test
    void shouldCleanMojibakeInAIRecommendationConstructor() {
        // Create AIRecommendation with mojibake in title and description
        LLMAnalysis.AIRecommendation rec = new LLMAnalysis.AIRecommendation(
                "Improve responseΓÇætime",
                "Community members are not receiving timely responses",
                80, 85, "🔴"
        );

        // Verify mojibake is cleaned
        assertThat(rec.getTitle()).doesNotContain("ΓÇæ");
        
        // Verify cleaned values
        assertThat(rec.getTitle()).isEqualTo("Improve response-time");
        assertThat(rec.getDescription()).contains("Community members are not receiving timely responses");
    }

    @Test
    void shouldHandleCleanTextInConstructors() {
        // Create objects with clean text (no mojibake)
        LLMAnalysis.ReadmeAnalysis readme = new LLMAnalysis.ReadmeAnalysis(
                7, 5, 6,
                List.of("Clean strength"),
                List.of("Clean suggestion")
        );

        LLMAnalysis.CommitAnalysis commit = new LLMAnalysis.CommitAnalysis(
                8, 6, 7,
                List.of("Clean pattern")
        );

        LLMAnalysis.CommunityAnalysis community = new LLMAnalysis.CommunityAnalysis(
                3, 3, 4,
                List.of("Clean strength"),
                List.of("Clean suggestion")
        );

        LLMAnalysis.AIRecommendation rec = new LLMAnalysis.AIRecommendation(
                "Clean title",
                "Clean description",
                80, 85, "🔴"
        );

        // Verify clean text remains unchanged
        assertThat(readme.getStrengths()).contains("Clean strength");
        assertThat(readme.getSuggestions()).contains("Clean suggestion");
        assertThat(commit.getPatterns()).contains("Clean pattern");
        assertThat(community.getStrengths()).contains("Clean strength");
        assertThat(community.getSuggestions()).contains("Clean suggestion");
        assertThat(rec.getTitle()).isEqualTo("Clean title");
        assertThat(rec.getDescription()).isEqualTo("Clean description");
    }

    @Test
    void shouldHandleEmptyListsInConstructors() {
        // Create objects with empty lists
        LLMAnalysis.ReadmeAnalysis readme = new LLMAnalysis.ReadmeAnalysis(
                7, 5, 6,
                List.of(),
                List.of()
        );

        LLMAnalysis.CommitAnalysis commit = new LLMAnalysis.CommitAnalysis(
                8, 6, 7,
                List.of()
        );

        LLMAnalysis.CommunityAnalysis community = new LLMAnalysis.CommunityAnalysis(
                3, 3, 4,
                List.of(),
                List.of()
        );

        // Verify empty lists are handled correctly
        assertThat(readme.getStrengths()).isEmpty();
        assertThat(readme.getSuggestions()).isEmpty();
        assertThat(commit.getPatterns()).isEmpty();
        assertThat(community.getStrengths()).isEmpty();
        assertThat(community.getSuggestions()).isEmpty();
    }

    @Test
    void shouldCleanComplexMojibakeInAllModels() {
        // Create all models with various mojibake patterns
        LLMAnalysis.ReadmeAnalysis readme = new LLMAnalysis.ReadmeAnalysis(
                7, 5, 6,
                List.of("ΓòÉΓòÉ Header ΓöÇΓöÇ", "Γû¬ Bullet"),
                List.of("firstΓÇæresponse", "24ΓÇô48")
        );

        LLMAnalysis.CommitAnalysis commit = new LLMAnalysis.CommitAnalysis(
                8, 6, 7,
                List.of("ΓòÉ Pattern", "ΓöÇ Separator", "Γû¬ Item")
        );

        LLMAnalysis.CommunityAnalysis community = new LLMAnalysis.CommunityAnalysis(
                3, 3, 4,
                List.of("ΓòÉ Strength", "ΓöÇ Another"),
                List.of("ΓÇæ Suggestion", "ΓÇô Another")
        );

        LLMAnalysis.AIRecommendation rec = new LLMAnalysis.AIRecommendation(
                "ΓòÉ Title ΓöÇ",
                "Γû¬ Description ΓÇæ",
                80, 85, "🔴"
        );

        // Verify all mojibake is cleaned
        assertThat(readme.getStrengths()).allMatch(s -> 
                !s.contains("ΓòÉ") && !s.contains("ΓöÇ") && !s.contains("Γû¬"));
        assertThat(readme.getSuggestions()).allMatch(s -> 
                !s.contains("ΓÇæ") && !s.contains("ΓÇô"));
        assertThat(commit.getPatterns()).allMatch(p -> 
                !p.contains("ΓòÉ") && !p.contains("ΓöÇ") && !p.contains("Γû¬"));
        assertThat(community.getStrengths()).allMatch(s -> 
                !s.contains("ΓòÉ") && !s.contains("ΓöÇ"));
        assertThat(community.getSuggestions()).allMatch(s -> 
                !s.contains("ΓÇæ") && !s.contains("ΓÇô"));
        assertThat(rec.getTitle()).doesNotContain("ΓòÉ", "ΓöÇ");
        assertThat(rec.getDescription()).doesNotContain("Γû¬", "ΓÇæ");
    }
}
