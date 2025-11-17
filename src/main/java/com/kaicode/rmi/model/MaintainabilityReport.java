package com.kaicode.rmi.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Final comprehensive analysis report for repository maintainability assessment.
 * <p>
 * This is the primary data structure representing the complete results of analyzing
 * a GitHub repository's maintainability. It aggregates individual metric scores into
 * an overall assessment with detailed breakdown and actionable recommendations.
 * <p>
 * The report includes:
 * <ul>
 *   <li>Repository identification and overall score</li>
 *   <li>Comprehensive breakdown by individual metrics</li>
 *   <li>Categorical rating for quick assessment (EXCELLENT, GOOD, FAIR, POOR, CRITICAL)</li>
 *   <li>Textual recommendations for repository improvement</li>
 * </ul>
 * <p>
 * The overall score is a weighted average of all metric results, providing
 * quantitative evaluation of repository health. A higher score indicates better
 * maintainability and lower risk of technical debt.
 * <p>
 * Instances are immutable and contain all final analysis results.
 * Used as the output format for {@link com.kaicode.rmi.service.MaintainabilityService}.
 * <p>
 * Rating thresholds:
 * <table border="1">
 *   <caption>Score to Rating Mapping</caption>
 *   <tr><th>Score Range</th><th>Rating</th><th>Description</th></tr>
 *   <tr><td>90+</td><td>EXCELLENT</td><td>Outstanding maintainability</td></tr>
 *   <tr><td>75-89</td><td>GOOD</td><td>Strong maintainability</td></tr>
 *   <tr><td>60-74</td><td>FAIR</td><td>Acceptable with room for improvement</td></tr>
 *   <tr><td>40-59</td><td>POOR</td><td>Needs significant attention</td></tr>
  *   <tr><td>&lt;40</td><td>CRITICAL</td><td>Immediate action required</td></tr>
 * </table>
 *
 * @since 1.0
 * @see com.kaicode.rmi.service.MaintainabilityService
 * @see MetricResult
 * @see com.kaicode.rmi.metrics.MetricCalculator
 */
public class MaintainabilityReport {
    private final String repositoryFullName;
    private final double overallScore;
    private final Map<String, MetricResult> metrics;
    private final String recommendation;

    /**
     * Private constructor for creating immutable MaintainabilityReport instances.
     * <p>
     * Called exclusively by {@link Builder#build()} to create validated,
     * immutable report objects. All final fields are assigned from validated
     * builder state. The metrics map is made unmodifiable to ensure immutability.
     *
     * @param builder validated builder containing all field values
     */
    private MaintainabilityReport(Builder builder) {
        this.repositoryFullName = builder.repositoryFullName;
        this.overallScore = builder.overallScore;
        this.metrics = Collections.unmodifiableMap(builder.metrics);
        this.recommendation = builder.recommendation;
    }

    /**
     * Gets the full name of the analyzed repository.
     * <p>
     * Returns the repository identifier in "owner/name" format.
     * This uniquely identifies which repository this report was generated for.
     *
     * @return repository full name in "owner/repository-name" format, never null
     */
    public String getRepositoryFullName() {
        return repositoryFullName;
    }

    /**
     * Gets the overall maintainability score for the repository.
     * <p>
     * This is a weighted average of all individual metric scores,
     * representing the overall health and maintainability of the repository.
     * Values range from 0.0 (worst) to 100.0 (perfect).
     *
     * @return overall score in range 0.0 to 100.0
     */
    public double getOverallScore() {
        return overallScore;
    }

    /**
     * Gets the detailed results for all individual metrics.
     * <p>
     * Returns an immutable map containing all metric calculations that contributed
     * to the overall score. The map key is the metric name, and the value contains
     * the score, weight, and additional details for each metric.
     *
     * @return immutable map of metric name to {@link MetricResult}, never null
     * @see MetricResult
     */
    public Map<String, MetricResult> getMetrics() {
        return metrics;
    }

    /**
     * Gets actionable recommendations for repository improvement.
     * <p>
     * Provides human-readable suggestions based on the analysis results.
     * Recommendations may include specific actions to improve maintainability,
     * address weak areas identified in the metrics, or best practices to adopt.
     *
     * @return improvement recommendations, may be null if no specific recommendations available
     */
    public String getRecommendation() {
        return recommendation;
    }

    /**
     * Gets a categorical rating based on the overall score.
     * <p>
     * Converts the numeric overall score into a qualitative assessment category
     * that provides immediate understanding of repository health:
     * <ul>
     *   <li>EXCELLENT: Score ≥ 90 - Outstanding maintainability</li>
     *   <li>GOOD: Score ≥ 75 - Strong maintainability</li>
     *   <li>FAIR: Score ≥ 60 - Acceptable with room for improvement</li>
     *   <li>POOR: Score ≥ 40 - Needs significant attention</li>
 *   <li>CRITICAL: Score &lt;40 - Immediate action required</li>
     * </ul>
     *
     * @return categorical rating based on score thresholds, never null
     */
    public String getRating() {
        if (overallScore >= 90) {
            return "EXCELLENT";
        } else if (overallScore >= 75) {
            return "GOOD";
        } else if (overallScore >= 60) {
            return "FAIR";
        } else if (overallScore >= 40) {
            return "POOR";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * Compares this maintainability report with another object for equality.
     * <p>
     * Two MaintainabilityReport objects are equal if they represent reports for the
     * same repository (repositoryFullName) and have identical overall scores.
     * Other fields are not considered to allow consistent identity based on core results.
     * <p>
     * Note: Immutable reports for the same repository with same results will be equal
     * even if generated at different times or containing different recommendations.
     *
     * @param o the object to compare with
     * @return true if both represent identical repository assessment results
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaintainabilityReport that = (MaintainabilityReport) o;
        return Double.compare(that.overallScore, overallScore) == 0 &&
                Objects.equals(repositoryFullName, that.repositoryFullName);
    }

    /**
     * Returns a hash code value for this maintainability report.
     * <p>
     * The hash code is computed from repositoryFullName and overallScore fields
     * to be consistent with the equals method implementation. This ensures proper
     * behavior when using reports as keys in hash-based collections.
     *
     * @return hash code for this maintainability report
     */
    @Override
    public int hashCode() {
        return Objects.hash(repositoryFullName, overallScore);
    }

    /**
     * Creates a new Builder instance for constructing MaintainabilityReport objects.
     * <p>
     * This factory method provides the entry point for the builder pattern,
     * allowing fluent construction of immutable MaintainabilityReport instances
     * with proper validation.
     *
     * @return a new Builder instance for method chaining
     * @since 1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing immutable MaintainabilityReport instances.
     * <p>
     * Provides a fluent API for setting all report fields before creating the
     * final immutable instance. The builder validates required fields
     * (repositoryFullName, metrics) and ensures correct state during building.
     * <p>
     * Required fields: repositoryFullName, metrics.<br>
     * Optional fields: overallScore, recommendation.
     *
     * @since 1.0
     * @see MaintainabilityReport
     */
    public static class Builder {
        private String repositoryFullName;
        private double overallScore;
        private Map<String, MetricResult> metrics;
        private String recommendation;

        /**
         * Sets the repository full name (required field).
         * <p>
         * The repository identifier must be in "owner/name" format.
         * This will be used to identify which repository the report pertains to.
         * This field is required and cannot be null.
         *
         * @param repositoryFullName the repository full name in "owner/name" format, must not be null
         * @return this builder instance for method chaining
         */
        public Builder repositoryFullName(String repositoryFullName) {
            this.repositoryFullName = repositoryFullName;
            return this;
        }

        /**
         * Sets the overall maintainability score (optional field).
         * <p>
         * This should be a weighted average of all individual metric scores.
         * Expected range is 0.0 (worst) to 100.0 (perfect).
         * If not set, defaults to current field value.
         *
         * @param overallScore the overall maintainability score in range 0.0 to 100.0
         * @return this builder instance for method chaining
         */
        public Builder overallScore(double overallScore) {
            this.overallScore = overallScore;
            return this;
        }

        /**
         * Sets the detailed metric results map (required field).
         * <p>
         * The map contains all individual metric calculations that contributed
         * to the overall score. Map keys should be metric names, values should be
         * MetricResult objects. This field is required and cannot be null.
         *
         * @param metrics immutable map of metric name to {@link MetricResult}, must not be null
         * @return this builder instance for method chaining
         */
        public Builder metrics(Map<String, MetricResult> metrics) {
            this.metrics = metrics;
            return this;
        }

        /**
         * Sets the improvement recommendations (optional field).
         * <p>
         * Human-readable suggestions for repository improvement based on the analysis.
         * Can include specific actions, best practices, or areas needing attention.
         *
         * @param recommendation improvement recommendations text, may be null
         * @return this builder instance for method chaining
         */
        public Builder recommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }

        /**
         * Builds and validates a new MaintainabilityReport instance.
         * <p>
         * Creates an immutable MaintainabilityReport from the current builder state.
         * Required fields (repositoryFullName, metrics) are validated during construction.
         * The metrics map is made unmodifiable to ensure immutability.
         * <p>
         * The returned instance is thread-safe and can be safely shared.
         *
         * @return a new immutable MaintainabilityReport instance
         * @throws NullPointerException if repositoryFullName or metrics is null
         */
        public MaintainabilityReport build() {
            Objects.requireNonNull(repositoryFullName, "repositoryFullName must not be null");
            Objects.requireNonNull(metrics, "metrics must not be null");
            return new MaintainabilityReport(this);
        }
    }
}
