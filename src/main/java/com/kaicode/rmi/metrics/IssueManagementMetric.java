package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;
import com.kaicode.rmi.model.RepositoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Metric calculator that evaluates issue tracking and management effectiveness.
 * <p>
 * This metric assesses how well a repository handles community-reported issues,
 * feature requests, and bug reports. Effective issue management indicates responsive
 * maintenance and active project stewardship. Projects with high issue closure rates
 * demonstrate commitment to addressing user concerns.
 * <p>
 * The metric evaluates three key aspects:
 * <ul>
 *   <li><strong>Issue closure rate</strong>: Percentage of closed vs total issues</li>
 *   <li><strong>Issue backlog health</strong>: Penalty for excessive open issues</li>
 *   <li><strong>Issue tracking availability</strong>: Special handling for disabled issues</li>
 * </ul>
 * <p>
 * Scoring based on closure rate:
 * <ul>
 *   <li>80%+ closure: 100 points (Excellent management)</li>
 *   <li>60-79% closure: 85 points (Good management)</li>
 *   <li>40-59% closure: 70 points (Fair management)</li>
 *   <li>20-39% closure: 50 points (Poor management)</li>
 *   <li>&lt;20% closure: 30 points (Critical backlog)</li>
 * </ul>
 * <p>
 * Additional penalties applied for excessive open issues (>100 = -20%, >50 = -10%).
 * Repositories with disabled issues receive neutral score of 50 (not evaluated).
 * Projects with no issues receive good score of 80 (unknown state treated positively).
 * <p>
 * Result contributes 20% to overall maintainability score (highest level metric).
 * Implementation analyzes issue statistics to assess project responsiveness.
 *
 * @since 1.0
 * @see MetricCalculator
 * @see GitHubClient#getRepository(String, String)
 * @see GitHubClient#getClosedIssuesCount(String, String)
 * @see RepositoryInfo#hasIssues()
 * @see RepositoryInfo#getOpenIssues()
 */
public class IssueManagementMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(IssueManagementMetric.class);
    private static final double WEIGHT = 0.20;

    /**
     * Calculates the issue management metric by analyzing issue tracking effectiveness.
     * <p>
     * Evaluates community issue handling by analyzing closure rates, backlog management,
     * and issue tracking setup. Handles special cases like disabled issues or new projects.
     * <p>
     * The assessment process:
     * <ol>
     *   <li>Check if issues are enabled for the repository</li>
     *   <li>If disabled, return neutral score (cannot evaluate)</li>
     *   <li>Get open and closed issue counts from repository metadata</li>
     *   <li>If no issues exist, assign good score (new project assumption)</li>
     *   <li>Calculate issue closure rate and apply backlog penalties</li>
     *   <li>Provide detailed breakdown of issue statistics</li>
     * </ol>
     *
     * @param client authenticated GitHub client for API access, never null
     * @param owner repository owner name, never null
     * @param repo repository name, never null
     * @return completed metric result with issue management score, description, and statistics details, never null
     * @throws IOException if network errors occur during GitHub API requests
     */
    @Override
    public MetricResult calculate(GitHubClient client, String owner, String repo) throws IOException {
        RepositoryInfo repoInfo = client.getRepository(owner, repo);

        if (!repoInfo.hasIssues()) {
            return MetricResult.builder()
                    .name(getMetricName())
                    .score(50.0)
                    .weight(getWeight())
                    .description("Evaluates issue tracking and management")
                    .details("Issues are disabled for this repository")
                    .build();
        }

        int openIssues = repoInfo.getOpenIssues();
        int closedIssues;
        try {
            closedIssues = client.getClosedIssuesCount(owner, repo);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("422")) {
                logger.warn("Large dataset detected for {}/{}, using estimation for closed issues", owner, repo);
                // For large repositories, estimate closed issues assuming typical 70% closure rate
                closedIssues = Math.max(0, (int)(openIssues / 0.3 * 0.7));
                logger.info("Estimated closed issues for {}/{}: {} (based on open issues: {})", owner, repo, closedIssues, openIssues);
            } else {
                throw e; // Re-throw non-422 errors
            }
        }
        int totalIssues = openIssues + closedIssues;

        if (totalIssues == 0) {
            return MetricResult.builder()
                    .name(getMetricName())
                    .score(80.0)
                    .weight(getWeight())
                    .description("Evaluates issue tracking and management")
                    .details("No issues found (may indicate new project or unused issue tracking)")
                    .build();
        }

        double closureRate = (closedIssues * 100.0) / totalIssues;
        double score = calculateScore(closureRate, openIssues);

        String details = String.format("Open: %d, Closed: %d (%.1f%% closure rate)",
                openIssues, closedIssues, closureRate);

        logger.info("Issue management score for {}/{}: {}", owner, repo, score);

        return MetricResult.builder()
                .name(getMetricName())
                .score(score)
                .weight(getWeight())
                .description("Evaluates issue tracking and management")
                .details(details)
                .build();
    }

    /**
     * Calculates issue management score based on closure rate and backlog health.
     * <p>
     * Applies tiered scoring based on issue closure rate, then adjusts for
     * excessive open issue backlog that may indicate maintenance problems.
     * <p>
     * Base scoring by closure rate:
     * <ul>
     *   <li>≥80%: 100.0 points</li>
     *   <li>≥60%: 85.0 points</li>
     *   <li>≥40%: 70.0 points</li>
     *   <li>≥20%: 50.0 points</li>
     *   <li>&lt;20%: 30.0 points</li>
     * </ul>
     * <p>
     * Backlog penalties:
     * <ul>
     *   <li>>100 open issues: -20% reduction</li>
     *   <li>>50 open issues: -10% reduction</li>
     *   <li>≤50 open issues: no penalty</li>
     * </ul>
     * <p>
     * This method encapsulates scoring logic for unit testing and maintainability.
     *
     * @param closureRate percentage of closed issues (0.0 to 100.0), must be valid ratio
     * @param openIssues number of currently open issues, must be non-negative
     * @return issue management score from 24.0 to 100.0 after backlog penalties
     */
    double calculateScore(double closureRate, int openIssues) {
        double baseScore;
        if (closureRate >= 80) {
            baseScore = 100.0;
        } else if (closureRate >= 60) {
            baseScore = 85.0;
        } else if (closureRate >= 40) {
            baseScore = 70.0;
        } else if (closureRate >= 20) {
            baseScore = 50.0;
        } else {
            baseScore = 30.0;
        }

        if (openIssues > 100) {
            baseScore *= 0.8;
        } else if (openIssues > 50) {
            baseScore *= 0.9;
        }

        return Math.min(100.0, baseScore);
    }

    /**
     * Returns the human-readable name of this metric.
     * <p>
     * The name identifies this metric as evaluating issue tracking responsiveness.
     *
     * @return string "Issue Management" as the metric name
     */
    @Override
    public String getMetricName() {
        return "Issue Management";
    }

    /**
     * Returns the relative importance weight of this issue management metric.
     * <p>
     * Issue management receives high weight (20%) reflecting its importance
     * for assessing project responsiveness and community support effectiveness.
     *
     * @return weight value 0.20 (20%)
     */
    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
