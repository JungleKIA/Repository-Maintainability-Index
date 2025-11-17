package com.kaicode.rmi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Comprehensive analysis results provided by Large Language Model (LLM) evaluation of repository quality.
 * <p>
 * This class encapsulates AI-powered analysis of various repository aspects including documentation
 * quality, commit message patterns, and community health indicators. The LLM provides intelligent
 * scoring and written feedback based on repository data and best practices.
 * <p>
 * The analysis includes four main components:
 * <ul>
 *   <li><strong>README Analysis</strong>: Quality assessment of project documentation including clarity,
 *       completeness, and newcomer-friendliness</li>
 *   <li><strong>Commit Analysis</strong>: Evaluation of commit message patterns for consistency and
 *       informativeness</li>
 *   <li><strong>Community Analysis</strong>: Assessment of community interaction quality and tone</li>
 *   <li><strong>AI Recommendations</strong>: Specific improvement suggestions with impact and confidence scores</li>
 * </ul>
 * <p>
 * All text content is automatically cleaned from encoding artifacts (mojibake) during construction
 * to ensure proper display on Windows systems and other environments.
 * <p>
 * Instances are immutable and incorporate confidence scoring and token usage tracking for
 * transparency in AI-powered analysis.
 * <p>
 * Usage in LLM analysis workflow:
 * <pre>{@code
 * LLMAnalysis analysis = LLMAnalysis.builder()
 *     .readmeAnalysis(new ReadmeAnalysis(85, 90, 78, strengths, suggestions))
 *     .commitAnalysis(new CommitAnalysis(75, 82, 68, patterns))
 *     .communityAnalysis(new CommunityAnalysis(88, 92, 85, commStrengths, commSuggestions))
 *     .recommendations(aiRecommendations)
 *     .confidence(0.87)
 *     .tokensUsed(1247)
 *     .build();
 * }</pre>
 *
 * @since 1.0
 * @see com.kaicode.rmi.llm.LLMAnalyzer
 * @see com.kaicode.rmi.util.LLMReportFormatter
 * @see com.kaicode.rmi.util.EncodingHelper#cleanTextForWindows(String)
 */
public class LLMAnalysis {
    private final ReadmeAnalysis readmeAnalysis;
    private final CommitAnalysis commitAnalysis;
    private final CommunityAnalysis communityAnalysis;
    private final List<AIRecommendation> recommendations;
    private final double confidence;
    private final int tokensUsed;

    /**
     * Private constructor for creating immutable LLMAnalysis instances.
     * <p>
     * Called exclusively by {@link Builder#build()} to create validated,
     * immutable analysis result objects. All final fields are assigned from
     * validated builder state. Collections are defensively copied to maintain immutability.
     *
     * @param builder validated builder containing all analysis components
     */
    private LLMAnalysis(Builder builder) {
        this.readmeAnalysis = builder.readmeAnalysis;
        this.commitAnalysis = builder.commitAnalysis;
        this.communityAnalysis = builder.communityAnalysis;
        this.recommendations = new ArrayList<>(builder.recommendations);
        this.confidence = builder.confidence;
        this.tokensUsed = builder.tokensUsed;
    }

    /**
     * Gets the comprehensive README file analysis performed by the LLM.
     * <p>
     * Returns detailed evaluation results including clarity scores, completeness assessment,
     * newcomer-friendliness rating, plus specific strengths and improvement suggestions.
     * All text content has been cleaned of encoding artifacts during construction.
     *
     * @return README analysis results containing scores and feedback, may be null if analysis not performed
     */
    public ReadmeAnalysis getReadmeAnalysis() {
        return readmeAnalysis;
    }

    /**
     * Gets the commit message pattern analysis performed by the LLM.
     * <p>
     * Returns evaluation of commit message quality including clarity, consistency,
     * informativeness ratings, and identified patterns used by the project.
     *
     * @return commit analysis results with scores and pattern identification, may be null if analysis not performed
     */
    public CommitAnalysis getCommitAnalysis() {
        return commitAnalysis;
    }

    /**
     * Gets the community health and interaction analysis performed by the LLM.
     * <p>
     * Returns assessment of community responsiveness, helpfulness, communication tone,
     * along with identified community strengths and improvement opportunities.
     * All text feedback has been cleaned of encoding artifacts.
     *
     * @return community analysis results with interaction quality scores and feedback, may be null if analysis not performed
     */
    public CommunityAnalysis getCommunityAnalysis() {
        return communityAnalysis;
    }

    /**
     * Gets the list of AI-generated recommendations for repository improvement.
     * <p>
     * Returns a defensive copy of all specific improvement suggestions provided by
     * the LLM, including impact assessment, confidence level, and severity classification.
     * Each recommendation provides actionable advice backed by AI reasoning.
     *
     * @return immutable list of AI recommendations with impact and confidence scores, never null
     */
    public List<AIRecommendation> getRecommendations() {
        return new ArrayList<>(recommendations);
    }

    /**
     * Gets the confidence level in the LLM analysis results.
     * <p>
     * Indicates statistical confidence in the analysis outcomes, typically ranging
     * from 0.0 (no confidence) to 1.0 (complete confidence). This helps users
     * understand the reliability of the AI-provided feedback and recommendations.
     *
     * @return confidence level in analysis results (0.0 to 1.0)
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Gets the total number of tokens consumed during the LLM analysis.
     * <p>
     * Reports the computational cost of the analysis in LLM tokens, providing
     * transparency into the AI processing requirements. Higher token counts
     * indicate more complex analysis performed.
     *
     * @return total tokens used by the LLM during analysis, always non-negative
     */
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
            // Clean text from mojibake at construction time
            this.strengths = strengths.stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(java.util.stream.Collectors.toList());
            this.suggestions = suggestions.stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(java.util.stream.Collectors.toList());
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
            // Clean text from mojibake at construction time
            this.patterns = patterns.stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(java.util.stream.Collectors.toList());
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
            // Clean text from mojibake at construction time
            this.strengths = strengths.stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(java.util.stream.Collectors.toList());
            this.suggestions = suggestions.stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(java.util.stream.Collectors.toList());
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
            // Clean text from mojibake at construction time
            this.title = com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows(
                    Objects.requireNonNull(title));
            this.description = com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows(
                    Objects.requireNonNull(description));
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
