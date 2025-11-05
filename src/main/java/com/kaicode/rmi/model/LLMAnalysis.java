package com.kaicode.rmi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LLMAnalysis {
    private final ReadmeAnalysis readmeAnalysis;
    private final CommitAnalysis commitAnalysis;
    private final CommunityAnalysis communityAnalysis;
    private final List<AIRecommendation> recommendations;
    private final double confidence;
    private final int tokensUsed;

    private LLMAnalysis(Builder builder) {
        this.readmeAnalysis = builder.readmeAnalysis;
        this.commitAnalysis = builder.commitAnalysis;
        this.communityAnalysis = builder.communityAnalysis;
        this.recommendations = new ArrayList<>(builder.recommendations);
        this.confidence = builder.confidence;
        this.tokensUsed = builder.tokensUsed;
    }

    public ReadmeAnalysis getReadmeAnalysis() {
        return readmeAnalysis;
    }

    public CommitAnalysis getCommitAnalysis() {
        return commitAnalysis;
    }

    public CommunityAnalysis getCommunityAnalysis() {
        return communityAnalysis;
    }

    public List<AIRecommendation> getRecommendations() {
        return new ArrayList<>(recommendations);
    }

    public double getConfidence() {
        return confidence;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ReadmeAnalysis readmeAnalysis;
        private CommitAnalysis commitAnalysis;
        private CommunityAnalysis communityAnalysis;
        private List<AIRecommendation> recommendations = new ArrayList<>();
        private double confidence;
        private int tokensUsed;

        public Builder readmeAnalysis(ReadmeAnalysis readmeAnalysis) {
            this.readmeAnalysis = readmeAnalysis;
            return this;
        }

        public Builder commitAnalysis(CommitAnalysis commitAnalysis) {
            this.commitAnalysis = commitAnalysis;
            return this;
        }

        public Builder communityAnalysis(CommunityAnalysis communityAnalysis) {
            this.communityAnalysis = communityAnalysis;
            return this;
        }

        public Builder recommendations(List<AIRecommendation> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder tokensUsed(int tokensUsed) {
            this.tokensUsed = tokensUsed;
            return this;
        }

        public LLMAnalysis build() {
            return new LLMAnalysis(this);
        }
    }

    public static class ReadmeAnalysis {
        private final int clarity;
        private final int completeness;
        private final int newcomerFriendly;
        private final List<String> strengths;
        private final List<String> suggestions;

        public ReadmeAnalysis(int clarity, int completeness, int newcomerFriendly,
                             List<String> strengths, List<String> suggestions) {
            this.clarity = clarity;
            this.completeness = completeness;
            this.newcomerFriendly = newcomerFriendly;
            this.strengths = new ArrayList<>(strengths);
            this.suggestions = new ArrayList<>(suggestions);
        }

        public int getClarity() {
            return clarity;
        }

        public int getCompleteness() {
            return completeness;
        }

        public int getNewcomerFriendly() {
            return newcomerFriendly;
        }

        public List<String> getStrengths() {
            return new ArrayList<>(strengths);
        }

        public List<String> getSuggestions() {
            return new ArrayList<>(suggestions);
        }
    }

    public static class CommitAnalysis {
        private final int clarity;
        private final int consistency;
        private final int informativeness;
        private final List<String> patterns;

        public CommitAnalysis(int clarity, int consistency, int informativeness, 
                             List<String> patterns) {
            this.clarity = clarity;
            this.consistency = consistency;
            this.informativeness = informativeness;
            this.patterns = new ArrayList<>(patterns);
        }

        public int getClarity() {
            return clarity;
        }

        public int getConsistency() {
            return consistency;
        }

        public int getInformativeness() {
            return informativeness;
        }

        public List<String> getPatterns() {
            return new ArrayList<>(patterns);
        }
    }

    public static class CommunityAnalysis {
        private final int responsiveness;
        private final int helpfulness;
        private final int tone;
        private final List<String> strengths;
        private final List<String> suggestions;

        public CommunityAnalysis(int responsiveness, int helpfulness, int tone,
                                List<String> strengths, List<String> suggestions) {
            this.responsiveness = responsiveness;
            this.helpfulness = helpfulness;
            this.tone = tone;
            this.strengths = new ArrayList<>(strengths);
            this.suggestions = new ArrayList<>(suggestions);
        }

        public int getResponsiveness() {
            return responsiveness;
        }

        public int getHelpfulness() {
            return helpfulness;
        }

        public int getTone() {
            return tone;
        }

        public List<String> getStrengths() {
            return new ArrayList<>(strengths);
        }

        public List<String> getSuggestions() {
            return new ArrayList<>(suggestions);
        }
    }

    public static class AIRecommendation {
        private final String title;
        private final String description;
        private final int impact;
        private final int confidence;
        private final String severity;

        public AIRecommendation(String title, String description, int impact, 
                               int confidence, String severity) {
            this.title = Objects.requireNonNull(title);
            this.description = Objects.requireNonNull(description);
            this.impact = impact;
            this.confidence = confidence;
            this.severity = Objects.requireNonNull(severity);
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getImpact() {
            return impact;
        }

        public int getConfidence() {
            return confidence;
        }

        public String getSeverity() {
            return severity;
        }
    }
}
