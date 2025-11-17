package com.kaicode.rmi.model;

import java.util.Objects;

/**
 * Results of calculating a specific repository maintainability metric.
 * <p>
 * This class contains complete information about the result of calculating one metric,
 * including the score, weight, description, and additional details. Used by all
 * implementations of {@link com.kaicode.rmi.metrics.MetricCalculator} to package
 * analysis results into a structured format.
 * <p>
 * Metric results include:
 * <ul>
 *   <li>Metric name for identification</li>
 *   <li>Score in range 0-100 (higher = better)</li>
 *   <li>Importance weight in range 0.0-1.0</li>
 *   <li>Brief description of what the metric measures</li>
 *   <li>Detailed calculation details and explanations</li>
 * </ul>
 * <p>
 * Instances are immutable and thread-safe.
 * Identity is based on metric name, score, and weight.
 * <p>
 * Usage:
 * <pre>{@code
 * MetricResult result = MetricResult.builder()
 *     .name("Commit Quality")
 *     .score(85.5)
 *     .weight(0.8)
 *     .description("Quality of commit messages")
 *     .details("Analyzed 50 commits, 85% have proper formatting")
 *     .build();
 * }</pre>
 *
 * @since 1.0
 * @see com.kaicode.rmi.metrics.MetricCalculator
 * @see com.kaicode.rmi.service.MaintainabilityService
 */
public class MetricResult {
    private final String name;
    private final double score;
    private final double weight;
    private final String description;
    private final String details;

    /**
     * Private constructor for creating immutable MetricResult instances.
     * <p>
     * Called exclusively by {@link Builder#build()} to create validated,
     * immutable metric result objects. All final fields are assigned from
     * the validated builder state.
     *
     * @param builder the builder containing validated field values
     */
    private MetricResult(Builder builder) {
        this.name = builder.name;
        this.score = builder.score;
        this.weight = builder.weight;
        this.description = builder.description;
        this.details = builder.details;
    }

    /**
     * Gets the metric name.
     * <p>
     * Human-readable name that identifies the metric.
     * Used for display in reports and interfaces.
     *
     * @return the metric name, never null
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the metric score.
     * <p>
     * Score value in range 0-100, where higher value
     * indicates better metric state. 100 = perfect.
     *
     * @return score in range 0.0 to 100.0
     */
    public double getScore() {
        return score;
    }

    /**
     * Gets the metric importance weight.
     * <p>
     * Weight indicates relative importance of this metric in the overall
     * maintainability calculation. Range 0.0-1.0, where 1.0 is most important.
     *
     * @return metric weight in range 0.0 to 1.0
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Gets the brief metric description.
     * <p>
     * Description explains what this metric specifically measures
     * and what repository quality aspects it evaluates.
     *
     * @return metric description, may be null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the detailed calculation details.
     * <p>
     * Contains additional information about how the score was calculated,
     * what data was analyzed, and specific results.
     *
     * @return calculation details, may be null
     */
    public String getDetails() {
        return details;
    }

    /**
     * Calculates the weighted metric score.
     * <p>
     * Weighted score = score × weight. This value reflects
     * the contribution of this metric to the overall maintainability score.
     * Used for aggregating results of all metrics.
     *
     * @return weighted score ({@code score × weight})
     */
    public double getWeightedScore() {
        return score * weight;
    }

    /**
     * Compares this metric result with another object for equality.
     * <p>
     * Two MetricResult objects are considered equal if they have identical
     * values for name, score, and weight. Other fields (description, details) are
     * not considered to support identity stability when calculation details change.
     *
     * @param o the object to compare with
     * @return true if objects represent the same metric result
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricResult that = (MetricResult) o;
        return Double.compare(that.score, score) == 0 &&
                Double.compare(that.weight, weight) == 0 &&
                Objects.equals(name, that.name);
    }

    /**
     * Returns a hash code value for this metric result.
     * <p>
     * Hash code is computed from name, score, and weight fields,
     * consistent with the equals method implementation.
     *
     * @return hash code for this metric result
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, score, weight);
    }

    /**
     * Creates a new Builder instance for constructing MetricResult objects.
     * <p>
     * This factory method provides the entry point for the builder pattern,
     * allowing fluent construction of immutable MetricResult instances
     * with proper validation.
     *
     * @return a new Builder instance for method chaining
     * @since 1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing immutable MetricResult instances.
     * <p>
     * Provides a fluent API for setting all metric result fields
     * before creating the final immutable instance. The builder validates
     * required fields (name) and value ranges during build.
     * <p>
     * Name field is required, other fields are optional.
     * Unset optional fields will receive default values.
     *
     * @since 1.0
     */
    public static class Builder {
        private String name;
        private double score;
        private double weight;
        private String description;
        private String details;

        /**
         * Sets the metric name (required field).
         * <p>
         * Human-readable name should uniquely identify the metric.
         * This field is required and cannot be null.
         *
         * @param name the metric name, must not be null or empty
         * @return this builder instance for method chaining
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the metric score.
         * <p>
         * Value should be in range 0.0 to 100.0, where higher value
         * indicates better metric state. 100.0 = perfect.
         * Value is validated during build.
         *
         * @param score the metric score in range 0.0 to 100.0
         * @return this builder instance for method chaining
         */
        public Builder score(double score) {
            this.score = score;
            return this;
        }

        /**
         * Sets the metric importance weight.
         * <p>
         * Weight indicates relative importance of metric in overall calculation.
         * Should be in range 0.0 to 1.0, where 1.0 means maximum importance.
         * Value is validated during build.
         *
         * @param weight the metric weight in range 0.0 to 1.0
         * @return this builder instance for method chaining
         */
        public Builder weight(double weight) {
            this.weight = weight;
            return this;
        }

        /**
         * Sets the brief metric description (optional field).
         * <p>
         * Description should explain what this metric measures
         * and what repository quality aspects it evaluates.
         *
         * @param description the metric description, may be null
         * @return this builder instance for method chaining
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the detailed calculation details (optional field).
         * <p>
         * Additional information about how the score was calculated,
         * what data was analyzed, and specific analysis results.
         *
         * @param details the calculation details, may be null
         * @return this builder instance for method chaining
         */
        public Builder details(String details) {
            this.details = details;
            return this;
        }

        /**
         * Builds and validates a new MetricResult instance.
         * <p>
         * Creates an immutable MetricResult from the current builder state.
         * Required fields (name) are validated for presence, and numeric fields
         * are validated for acceptable ranges.
         * <p>
         * The returned instance is thread-safe and can be safely shared.
         *
         * @return a new immutable MetricResult instance
         * @throws NullPointerException if name is null
         * @throws IllegalArgumentException if score is outside 0-100 range
         *                                  or weight is outside 0.0-1.0 range
         */
        public MetricResult build() {
            Objects.requireNonNull(name, "name must not be null");
            if (score < 0 || score > 100) {
                throw new IllegalArgumentException("score must be between 0 and 100");
            }
            if (weight < 0 || weight > 1) {
                throw new IllegalArgumentException("weight must be between 0 and 1");
            }
            return new MetricResult(this);
        }
    }
}
