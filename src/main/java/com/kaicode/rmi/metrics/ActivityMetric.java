package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.MetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Metric calculator that evaluates repository activity and freshness based on commit recency.
 * <p>
 * This metric assesses how actively a repository is maintained by analyzing the time elapsed
 * since the most recent commit. Active repositories with recent commits indicate healthy maintenance
 * practices and responsiveness to the community.
 * <p>
 * The metric evaluates the freshness of recent commits by calculating days since the latest commit
 * and applying a tiered scoring system that favors more recent activity:
 * <ul>
 *   <li>Very Active: ≤ 7 days - 100 points</li>
 *   <li>Active: 8-30 days - 90 points</li>
 *   <li>Moderate: 31-90 days - 70 points</li>
 *   <li>Low: 91-180 days - 50 points</li>
 *   <li>Inactive: 181-365 days - 30 points</li>
 *   <li>Abandoned: > 365 days - 10 points</li>
 * </ul>
 * <p>
 * A higher score indicates more recent and active maintenance of the repository.
 * Result contributes 15% to overall maintainability score.
 * <p>
 * Implementation analyzes the 10 most recent commits to determine activity level.
 * Empty repositories receive a score of 0.
 *
 * @since 1.0
 * @see MetricCalculator
 * @see GitHubClient#getRecentCommits(String, String, int)
 */
public class ActivityMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(ActivityMetric.class);
    private static final double WEIGHT = 0.15;
    private static final int RECENT_COMMITS_COUNT = 10;

    /**
     * Calculates the activity metric by analyzing recent commit recency.
     * <p>
     * Retrieves the 10 most recent commits and evaluates repository freshness based on
     * the time elapsed since the latest commit. Active repositories with recent commits
     * receive higher scores. Empty repositories receive a score of 0.
     * <p>
     * The calculation process:
     * <ol>
     *   <li>Fetch 10 most recent commits</li>
     *   <li>If no commits exist, return 0 score</li>
     *   <li>Calculate days since latest commit</li>
     *   <li>Apply tiered scoring based on recency</li>
     *   <li>Include activity details in result</li>
     * </ol>
     *
     * @param client authenticated GitHub client for API access, never null
     * @param owner repository owner name, never null
     * @param repo repository name, never null
     * @return completed metric result with score, description, and activity details, never null
     * @throws IOException if network errors occur during GitHub API requests
     */
    @Override
    public MetricResult calculate(GitHubClient client, String owner, String repo) throws IOException {
        List<CommitInfo> commits = client.getRecentCommits(owner, repo, RECENT_COMMITS_COUNT);

        if (commits.isEmpty()) {
            return MetricResult.builder()
                    .name(getMetricName())
                    .score(0)
                    .weight(getWeight())
                    .description("Evaluates repository activity and freshness")
                    .details("No commits found")
                    .build();
        }

        LocalDateTime mostRecentCommit = commits.get(0).getDate();
        LocalDateTime now = LocalDateTime.now();
        long daysSinceLastCommit = ChronoUnit.DAYS.between(mostRecentCommit, now);

        double score = calculateScoreFromDays(daysSinceLastCommit);

        String details = String.format("Last commit was %d days ago. Recent activity: %d commits",
                daysSinceLastCommit, commits.size());

        logger.info("Activity score for {}/{}: {}", owner, repo, score);

        return MetricResult.builder()
                .name(getMetricName())
                .score(score)
                .weight(getWeight())
                .description("Evaluates repository activity and freshness")
                .details(details)
                .build();
    }

    /**
     * Calculates activity score from days since last commit using tiered evaluation.
     * <p>
     * Applies a decreasing score based on how many days have passed since the last commit:
     * <ul>
     *   <li>≤ 7 days: 100.0 (Very Active)</li>
     *   <li>≤ 30 days: 90.0 (Active)</li>
     *   <li>≤ 90 days: 70.0 (Moderate)</li>
     *   <li>≤ 180 days: 50.0 (Low)</li>
     *   <li>≤ 365 days: 30.0 (Inactive)</li>
     *   <li>> 365 days: 10.0 (Abandoned)</li>
     * </ul>
     * <p>
     * This method encapsulates the scoring logic for unit testing and maintainability.
     *
     * @param days number of days since the most recent commit, must be non-negative
     * @return activity score from 10.0 to 100.0 based on recency tier
     */
    double calculateScoreFromDays(long days) {
        if (days <= 7) {
            return 100.0;
        } else if (days <= 30) {
            return 90.0;
        } else if (days <= 90) {
            return 70.0;
        } else if (days <= 180) {
            return 50.0;
        } else if (days <= 365) {
            return 30.0;
        } else {
            return 10.0;
        }
    }

    /**
     * Returns the human-readable name of this metric.
     * <p>
     * The name identifies this metric as evaluating repository activity levels.
     *
     * @return string "Activity" as the metric name
     */
    @Override
    public String getMetricName() {
        return "Activity";
    }

    /**
     * Returns the relative importance weight of this activity metric.
     * <p>
     * Activity contributes 15% to the overall maintainability score,
     * reflecting its importance in assessing repository health.
     *
     * @return weight value 0.15 (15%)
     */
    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
