package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Metric calculator that evaluates branch management and cleanup practices.
 * <p>
 * This metric assesses how well repository branches are managed by analyzing the total
 * number of active branches. Repositories with too many branches suggest poor maintenance
 * practices, while well-managed repositories keep branch count manageable.
 * <p>
 * The metric applies tiered scoring based on branch count that favors fewer,
 * well-organized branches:
 * <ul>
 *   <li>Minimal: ≤ 3 branches - 100 points</li>
 *   <li>Good: 4-5 branches - 95 points</li>
 *   <li>Acceptable: 6-10 branches - 85 points</li>
 *   <li>Moderate issues: 11-20 branches - 70 points</li>
 *   <li>Poor management: 21-50 branches - 50 points</li>
 *   <li>Disorganized: > 50 branches - 30 points</li>
 * </ul>
 * <p>
 * A higher score indicates better branch management and cleaner repository structure.
 * Result contributes 15% to overall maintainability score.
 * <p>
 * Implementation analyzes total branch count to assess organizational practices.
 *
 * @since 1.0
 * @see MetricCalculator
 * @see GitHubClient#getBranchCount(String, String)
 */
public class BranchManagementMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(BranchManagementMetric.class);
    private static final double WEIGHT = 0.15;

    /**
     * Calculates the branch management metric by analyzing total branch count.
     * <p>
     * Retrieves the total number of branches in the repository and applies
     * tiered scoring that favors repositories with manageable branch counts.
     * Fewer, well-organized branches indicate better maintenance practices.
     *
     * @param client authenticated GitHub client for API access, never null
     * @param owner repository owner name, never null
     * @param repo repository name, never null
     * @return completed metric result with score, description, and branch count details, never null
     * @throws IOException if network errors occur during GitHub API requests
     */
    @Override
    public MetricResult calculate(GitHubClient client, String owner, String repo) throws IOException {
        int branchCount = client.getBranchCount(owner, repo);

        double score = calculateScore(branchCount);

        String details = String.format("Total branches: %d", branchCount);

        logger.info("Branch management score for {}/{}: {}", owner, repo, score);

        return MetricResult.builder()
                .name(getMetricName())
                .score(score)
                .weight(getWeight())
                .description("Evaluates branch management and cleanup practices")
                .details(details)
                .build();
    }

    /**
     * Calculates branch management score based on branch count using tiered evaluation.
     * <p>
     * Applies decreasing scores based on total branch count:
     * <ul>
     *   <li>≤ 3 branches: 100.0 (Minimal)</li>
     *   <li>≤ 5 branches: 95.0 (Good)</li>
     *   <li>≤ 10 branches: 85.0 (Acceptable)</li>
     *   <li>≤ 20 branches: 70.0 (Moderate issues)</li>
     *   <li>≤ 50 branches: 50.0 (Poor management)</li>
     *   <li>> 50 branches: 30.0 (Disorganized)</li>
     * </ul>
     * <p>
     * This method encapsulates the scoring logic for unit testing and maintainability.
     *
     * @param branchCount total number of branches in repository, must be non-negative
     * @return branch management score from 30.0 to 100.0 based on branch count
     */
    double calculateScore(int branchCount) {
        if (branchCount <= 3) {
            return 100.0;
        } else if (branchCount <= 5) {
            return 95.0;
        } else if (branchCount <= 10) {
            return 85.0;
        } else if (branchCount <= 20) {
            return 70.0;
        } else if (branchCount <= 50) {
            return 50.0;
        } else {
            return 30.0;
        }
    }

    /**
     * Returns the human-readable name of this metric.
     * <p>
     * The name identifies this metric as evaluating repository branch management practices.
     *
     * @return string "Branch Management" as the metric name
     */
    @Override
    public String getMetricName() {
        return "Branch Management";
    }

    /**
     * Returns the relative importance weight of this branch management metric.
     * <p>
     * Branch management contributes 15% to the overall maintainability score,
     * reflecting its importance in assessing organizational practices.
     *
     * @return weight value 0.15 (15%)
     */
    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
