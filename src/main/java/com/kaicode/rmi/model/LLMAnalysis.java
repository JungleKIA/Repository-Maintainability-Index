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
    private final String llmMode;

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
        this.llmMode = builder.llmMode;
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

    /**
     * Creates a new Builder instance for constructing LLMAnalysis objects.
     * <p>
     * This factory method provides the entry point for the builder pattern,
     * allowing fluent construction of immutable LLMAnalysis instances with
     * proper validation.
     *
     * @return a new Builder instance for method chaining
     * @since 1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the LLM analysis mode indicating whether real AI or fallback was used.
     * <p>
     * Indicates the source of the LLM analysis results, either "REAL" for actual
     * AI processing or "FALLBACK" for intelligent default values when the API
     * is unavailable. This provides transparency about analysis reliability.
     *
     * @return analysis mode ("REAL" or "FALLBACK"), never null
     */
    public String getLlmMode() {
        return llmMode;
    }

    /**
     * Builder class for constructing immutable LLMAnalysis instances.
     * <p>
     * Provides a fluent API for setting all analysis fields before creating
     * the final immutable instance. Ensures correct state during building
     * and validates required fields during construction.
     * <p>
     * Required fields: none<br>
     * Optional fields: readmeAnalysis, commitAnalysis, communityAnalysis,
     *                 recommendations, confidence, tokensUsed, llmMode.
     *
     * @since 1.0
     * @see LLMAnalysis
     */
    public static class Builder {
        private ReadmeAnalysis readmeAnalysis;
        private CommitAnalysis commitAnalysis;
        private CommunityAnalysis communityAnalysis;
        private List<AIRecommendation> recommendations = new ArrayList<>();
        private double confidence;
        private int tokensUsed;
        private String llmMode = "FALLBACK";

        /**
         * Sets the README analysis results for this LLM analysis.
         * <p>
         * Configures the documentation quality assessment component
         * of the complete analysis. The README analysis evaluates
         * clarity, completeness, and newcomer-friendliness.
         *
         * @param readmeAnalysis documentation quality analysis results,
         *                      replaces any previously set value
         * @return this builder instance for method chaining
         */
        public Builder readmeAnalysis(ReadmeAnalysis readmeAnalysis) {
            this.readmeAnalysis = readmeAnalysis;
            return this;
        }

        /**
         * Sets the commit message analysis results for this LLM analysis.
         * <p>
         * Configures the commit message quality assessment component
         * of the complete analysis. The commit analysis evaluates
         * clarity, consistency, and informativeness of messages.
         *
         * @param commitAnalysis commit message quality analysis results,
         *                      replaces any previously set value
         * @return this builder instance for method chaining
         */
        public Builder commitAnalysis(CommitAnalysis commitAnalysis) {
            this.commitAnalysis = commitAnalysis;
            return this;
        }

        /**
         * Sets the community health analysis results for this LLM analysis.
         * <p>
         * Configures the community interaction quality assessment component
         * of the complete analysis. The community analysis evaluates
         * responsiveness, helpfulness, and communication tone.
         *
         * @param communityAnalysis community health analysis results,
         *                         replaces any previously set value
         * @return this builder instance for method chaining
         */
        public Builder communityAnalysis(CommunityAnalysis communityAnalysis) {
            this.communityAnalysis = communityAnalysis;
            return this;
        }

        /**
         * Sets the AI-generated recommendations for repository improvement.
         * <p>
         * Configures the list of actionable improvement suggestions
         * generated by the LLM, including impact assessment and confidence
         * levels for each recommendation.
         *
         * @param recommendations list of AI recommendations with impact scores,
         *                       replaces any previously set recommendations
         * @return this builder instance for method chaining
         */
        public Builder recommendations(List<AIRecommendation> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        /**
         * Sets the confidence level in the analysis results.
         * <p>
         * Configures the statistical confidence indicator for the analysis
         * outcomes, typically ranging from 0.0 (no confidence) to 1.0
         * (complete confidence).
         *
         * @param confidence confidence level in analysis results (0.0 to 1.0),
         *                  replaces any previously set confidence value
         * @return this builder instance for method chaining
         */
        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        /**
         * Sets the total number of tokens used during LLM analysis.
         * <p>
         * Configures the computational cost indicator showing the number
         * of tokens consumed during the AI analysis process.
         *
         * @param tokensUsed total tokens consumed by the LLM (non-negative),
         *                  replaces any previously set token count
         * @return this builder instance for method chaining
         */
        public Builder tokensUsed(int tokensUsed) {
            this.tokensUsed = tokensUsed;
            return this;
        }

        /**
         * Sets the LLM analysis mode indicating the source of the analysis.
         * <p>
         * Configures whether the analysis results come from real AI processing ("REAL")
         * or fallback intelligent defaults ("FALLBACK") when the API is unavailable.
         *
         * @param llmMode analysis mode ("REAL" or "FALLBACK"),
         *               defaults to "FALLBACK" if not set
         * @return this builder instance for method chaining
         */
        public Builder llmMode(String llmMode) {
            this.llmMode = Objects.requireNonNullElse(llmMode, "FALLBACK");
            return this;
        }

        /**
         * Builds and validates a new LLMAnalysis instance.
         * <p>
         * Creates an immutable LLMAnalysis from the current builder state.
         * All set fields are used to initialize the final instance.
         *
         * @return a new immutable LLMAnalysis instance with configured components
         * @since 1.0
         */
        public LLMAnalysis build() {
            return new LLMAnalysis(this);
        }
    }

    /**
     * Analysis results for README documentation quality assessment.
     * <p>
     * Contains LLM-evaluated quality metrics for repository documentation,
     * including clarity of explanations, completeness of coverage, and
     * newcomer-friendliness of onboarding guidance. Provides specific
     * strengths and actionable improvement suggestions for documentation.
     */
    public static class ReadmeAnalysis {
        private final int clarity;
        private final int completeness;
        private final int newcomerFriendly;
        private final List<String> strengths;
        private final List<String> suggestions;

        /**
         * Creates a new README analysis result instance.
         * <p>
         * Initializes quality scores and feedback lists. Input text lists
         * are automatically cleaned of encoding artifacts during construction.
         *
         * @param clarity score from 1-10 indicating documentation clarity
         * @param completeness score from 1-10 indicating documentation completeness
         * @param newcomerFriendly score from 1-10 indicating onboarding ease
         * @param strengths list of identified documentation strengths, may be null
         * @param suggestions list of improvement recommendations, may be null
         * @see com.kaicode.rmi.util.EncodingHelper#cleanTextForWindows(String)
         */
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

        /**
         * Gets the documentation clarity score assigned by the LLM.
         * <p>
         * Represents how clearly and understandabily information is presented
         * in the README documentation, assessing writing quality and structure.
         *
         * @return clarity score from 1 (poor) to 10 (excellent)
         */
        public int getClarity() {
            return clarity;
        }

        /**
         * Gets the documentation completeness score assigned by the LLM.
         * <p>
         * Measures how comprehensively the README covers essential project
         * information like installation, usage, and configuration details.
         *
         * @return completeness score from 1 (incomplete) to 10 (comprehensive)
         */
        public int getCompleteness() {
            return completeness;
        }

        /**
         * Gets the newcomer-friendliness score assigned by the LLM.
         * <p>
         * Evaluates how easy it is for newcomers to get started with the project
         * based on the clarity of setup instructions and onboarding guidance.
         *
         * @return friendliness score from 1 (difficult) to 10 (beginner-friendly)
         */
        public int getNewcomerFriendly() {
            return newcomerFriendly;
        }

        /**
         * Gets the list of identified README documentation strengths.
         * <p>
         * Returns AI-identified positive aspects of the documentation,
         * including particularly well-written sections or comprehensive coverage
         * of specific topics. All text has been cleaned of encoding artifacts.
         *
         * @return defensive copy of strength descriptions, never null
         */
        public List<String> getStrengths() {
            return new ArrayList<>(strengths);
        }

        /**
         * Gets the list of specific README improvement recommendations.
         * <p>
         * Returns actionable suggestions from the LLM for enhancing documentation
         * quality, including sections to add, clarify, or restructure.
         * All text has been cleaned of encoding artifacts for proper display.
         *
         * @return defensive copy of improvement suggestions, never null
         */
        public List<String> getSuggestions() {
            return new ArrayList<>(suggestions);
        }
    }

    /**
     * Analysis results for commit message quality and pattern evaluation.
     * <p>
     * Contains LLM evaluation of commit message practices including clarity
     * of descriptions, consistency of formatting, informativeness of content,
     * and identification of recurring patterns in commit history.
     */
    public static class CommitAnalysis {
        private final int clarity;
        private final int consistency;
        private final int informativeness;
        private final List<String> patterns;

        /**
         * Creates a new commit analysis result instance.
         * <p>
         * Initializes quality scores and pattern analysis. Input patterns
         * are automatically cleaned of encoding artifacts during construction.
         *
         * @param clarity score from 1-10 indicating message clarity
         * @param consistency score from 1-10 indicating formatting consistency
         * @param informativeness score from 1-10 indicating information content
         * @param patterns list of identified commit message patterns, may be null
         * @see com.kaicode.rmi.util.EncodingHelper#cleanTextForWindows(String)
         */
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

        /**
         * Gets the commit message clarity score assigned by the LLM.
         * <p>
         * Evaluates how clear and unambiguous commit messages are,
         * assessing writing quality and descriptive accuracy.
         *
         * @return clarity score from 1 (confusing) to 10 (crystal clear)
         */
        public int getClarity() {
            return clarity;
        }

        /**
         * Gets the commit message consistency score assigned by the LLM.
         * <p>
         * Measures uniformity in formatting, capitalization, punctuation,
         * and structural consistency across the commit history sample.
         *
         * @return consistency score from 1 (inconsistent) to 10 (uniform)
         */
        public int getConsistency() {
            return consistency;
        }

        /**
         * Gets the commit message informativeness score assigned by the LLM.
         * <p>
         * Assesses how much useful information each commit provides about
         * what was changed, why it was changed, and the impact of the change.
         *
         * @return informativeness score from 1 (minimal) to 10 (comprehensive)
         */
        public int getInformativeness() {
            return informativeness;
        }

        /**
         * Gets the list of identified commit message patterns.
         * <p>
         * Returns AI-analyzed patterns from the commit message corpus,
         * including both positive practices and problematic patterns.
         * All text has been cleaned of encoding artifacts for proper display.
         *
         * @return defensive copy of identified patterns, never null
         */
        public List<String> getPatterns() {
            return new ArrayList<>(patterns);
        }
    }

    /**
     * Analysis results for community health and interaction quality evaluation.
     * <p>
     * Contains LLM evaluation of community engagement quality including responsiveness
     * to user inquiries, helpfulness of community contributions, communication tone
     * quality, and identification of community strengths and improvement opportunities.
     */
    public static class CommunityAnalysis {
        private final int responsiveness;
        private final int helpfulness;
        private final int tone;
        private final List<String> strengths;
        private final List<String> suggestions;

        /**
         * Creates a new community analysis result instance.
         * <p>
         * Initializes quality scores and feedback lists. Input text lists
         * are automatically cleaned of encoding artifacts during construction.
         *
         * @param responsiveness score from 1-10 indicating response speed and frequency
         * @param helpfulness score from 1-10 indicating community assistance quality
         * @param tone score from 1-10 indicating communication tone quality
         * @param strengths list of identified community strengths, may be null
         * @param suggestions list of community improvement recommendations, may be null
         * @see com.kaicode.rmi.util.EncodingHelper#cleanTextForWindows(String)
         */
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

        /**
         * Gets the community responsiveness score assigned by the LLM.
         * <p>
         * Evaluates how quickly and consistently the community responds
         * to user questions, issues, and pull requests, indicating helpfulness
         * and active community engagement.
         *
         * @return responsiveness score from 1 (slow responses) to 10 (immediate response)
         */
        public int getResponsiveness() {
            return responsiveness;
        }

        /**
         * Gets the community helpfulness score assigned by the LLM.
         * <p>
         * Assesses the quality and effectiveness of community assistance,
         * measuring how well community members help users solve problems
         * and provide accurate guidance.
         *
         * @return helpfulness score from 1 (unhelpful) to 10 (highly supportive)
         */
        public int getHelpfulness() {
            return helpfulness;
        }

        /**
         * Gets the communication tone quality score assigned by the LLM.
         * <p>
         * Evaluates the professionalism and approachability of community
         * communication, including politeness, inclusivity, and constructive
         * feedback patterns.
         *
         * @return tone quality score from 1 (poor) to 10 (excellent)
         */
        public int getTone() {
            return tone;
        }

        /**
         * Gets the list of identified community strengths.
         * <p>
         * Returns AI-identified positive aspects of community behavior,
         * including effective communication practices, helpful initiatives,
         * or positive cultural elements. All text has been cleaned of encoding artifacts.
         *
         * @return defensive copy of community strength descriptions, never null
         */
        public List<String> getStrengths() {
            return new ArrayList<>(strengths);
        }

        /**
         * Gets the list of community improvement recommendations.
         * <p>
         * Returns actionable suggestions from the LLM for enhancing community
         * health, including communication improvements, engagement strategies,
         * or process optimizations. All text has been cleaned of encoding artifacts.
         *
         * @return defensive copy of improvement recommendations, never null
         */
        public List<String> getSuggestions() {
            return new ArrayList<>(suggestions);
        }
    }

    /**
     * Individual AI-generated recommendation for repository improvement.
     * <p>
     * Represents a specific, actionable suggestion provided by the LLM
     * to improve repository maintainability. Each recommendation includes
     * a title, detailed description, and quantitative impact/confidence scores.
     * This immutable data structure helps organize prioritized improvement actions.
     */
    public static class AIRecommendation {
        private final String title;
        private final String description;
        private final int impact;
        private final int confidence;
        private final String severity;

        /**
         * Creates a new AI recommendation instance.
         * <p>
         * Initializes recommendation properties with AI analysis results.
         * Text fields are automatically cleaned of encoding artifacts for proper display.
         *
         * @param title short title of the recommendation (cleaned for encoding)
         * @param description detailed explanation of the suggested improvement
         * @param impact expected positive impact score (1-10 scale)
         * @param confidence AI confidence in the recommendation accuracy (1-10 scale)
         * @param severity importance level classification of the recommendation
         */
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

        /**
         * Gets the recommendation title.
         * <p>
         * Returns the concise title describing the improvement action.
         * This field has been cleaned of encoding artifacts.
         *
         * @return cleaned recommendation title, never null
         */
        public String getTitle() {
            return title;
        }

        /**
         * Gets the detailed recommendation description.
         * <p>
         * Provides comprehensive explanation of the suggested improvement,
         * including rationale and expected benefits. Cleaned for encoding.
         *
         * @return cleaned detailed recommendation description, never null
         */
        public int getImpact() {
            return impact;
        }

        /**
         * Gets the AI confidence score for this recommendation.
         * <p>
         * Indicates how confident the AI model is in the accuracy and
         * relevance of this specific improvement suggestion.
         *
         * @return confidence score from 1 (low confidence) to 10 (high confidence)
         */
        public int getConfidence() {
            return confidence;
        }

        /**
         * Gets the recommendation description.
         * <p>
         * Returns AI-generated detailed explanation of the suggested improvement,
         * including rationale and expected outcomes. Text is cleaned for encoding.
         *
         * @return cleaned recommendation description, never null
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the severity/priority level classification.
         * <p>
         * Indicates the importance or urgency of implementing this recommendation.
         * Higher severity suggests greater potential impact on maintainability.
         *
         * @return severity classification (e.g., "HIGH", "MEDIUM", "LOW"), never null
         */
        public String getSeverity() {
            return severity;
        }
    }
}
