package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;

import java.io.IOException;

/**
 * Calculator interface for computing maintainability metrics on GitHub repositories.
 * <p>
 * Implementations of this interface provide specific metrics that analyze different
 * aspects of repository quality, such as documentation quality, commit patterns,
 * community activity, and other maintainability factors.
 * <p>
 * Each metric calculator has:
 * <ul>
 *   <li>A descriptive name identifying the metric being calculated</li>
 *   <li>A weight value indicating relative importance (0.0 to 1.0)</li>
 *   <li>The ability to analyze repository data and return scored results</li>
 * </ul>
 * <p>
 * Metric calculators should be thread-safe and stateless to allow parallel
 * execution across multiple repositories.
 *
 * @since 1.0
 * @see MetricResult
 * @see com.kaicode.rmi.service.MaintainabilityService
 */
public interface MetricCalculator {
    /**
     * Calculates the specific maintainability metric for a GitHub repository.
     * <p>
     * This is the core method where metric calculations are performed.
     * Implementations should analyze repository data through the provided
     * GitHub client, compute a score, and return detailed results.
     * <p>
     * The calculation may involve multiple API calls and complex analysis
     * of repository structure, commit history, and community data.
     *
     * @param client authenticated GitHub client for API access, never null
     * @param owner repository owner username or organization, never null
     * @param repo repository name, never null
     * @return calculated metric result with score, details, and metadata, never null
     * @throws IOException if network errors occur or API limits exceeded
     */
    MetricResult calculate(GitHubClient client, String owner, String repo) throws IOException;

    /**
     * Returns the human-readable name of this metric.
     * <p>
     * The name should be descriptive and indicate what aspect of maintainability
     * this metric evaluates. Examples: "Documentation Quality", "Commit Activity".
     *
     * @return metric name, never null or empty
     */
    String getMetricName();

    /**
     * Returns the relative importance weight of this metric.
     * <p>
     * Weight values indicate how important this metric is in the overall
     * maintainability calculation. Higher weights contribute more to the
     * final score. Typical range: 0.1 to 1.0.
     *
     * @return metric weight as decimal value, usually between 0.0 and 1.0
     */
    double getWeight();
}
