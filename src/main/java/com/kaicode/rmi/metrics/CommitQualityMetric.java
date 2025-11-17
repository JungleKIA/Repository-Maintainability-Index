package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.MetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Metric calculator that evaluates commit message quality and adherence to conventions.
 * <p>
 * This metric assesses how well commit messages follow best practices by analyzing
 * message content, structure, and adherence to conventional commit standards.
 * Good commit practices indicate disciplined development processes and better
 * project maintainability for future contributors.
 * <p>
 * The metric evaluates each commit message against quality criteria including:
 * <ul>
 *   <li>Conventional commit prefixes (feat:, fix:, docs:, refactor:, etc.)</li>
 *   <li>Proper message length (minimum 10 chars, preferred 20+)</li>
 *   <li>Capitalization and professional tone</li>
 *   <li>Avoidance of merge commits, WIP indicators, and generic messages</li>
 * </ul>
 * <p>
 * Final score represents the percentage of commits that meet quality standards
 * (0-100%, where higher indicates better commit practices).
 * Result contributes 15% to overall maintainability score.
 * <p>
 * Implementation analyzes up to 50 most recent commits to assess message quality.
 * Empty repositories receive a score of 0.
 *
 * @since 1.0
 * @see MetricCalculator
 * @see GitHubClient#getRecentCommits(String, String, int)
 * @see <a href="https://www.conventionalcommits.org/">Conventional Commits Specification</a>
 */
public class CommitQualityMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(CommitQualityMetric.class);
    private static final double WEIGHT = 0.15;
    private static final int COMMITS_TO_ANALYZE = 50;
    private static final Pattern GOOD_COMMIT_PATTERN = Pattern.compile(
            "^(feat|fix|docs|style|refactor|test|chore|perf|ci|build)(\\(.+\\))?:.+", 
            Pattern.CASE_INSENSITIVE);

    /**
     * Calculates the commit quality metric by analyzing recent commit messages.
     * <p>
     * Retrieves up to 50 most recent commits and evaluates the percentage that follow
     * commit message best practices. Higher scores indicate better development practices.
     * Empty repositories receive a score of 0.
     * <p>
     * The assessment process:
     * <ol>
     *   <li>Fetch up to 50 most recent commits</li>
     *   <li>If no commits exist, return 0 score</li>
     *   <li>Analyze each commit message for quality indicators</li>
     *   <li>Calculate percentage of commits meeting quality standards</li>
     *   <li>Provide detailed breakdown of results</li>
     * </ol>
     *
     * @param client authenticated GitHub client for API access, never null
     * @param owner repository owner name, never null
     * @param repo repository name, never null
     * @return completed metric result with quality score, description, and commit analysis details, never null
     * @throws IOException if network errors occur during GitHub API requests
     */
    @Override
    public MetricResult calculate(GitHubClient client, String owner, String repo) throws IOException {
        List<CommitInfo> commits = client.getRecentCommits(owner, repo, COMMITS_TO_ANALYZE);

        if (commits.isEmpty()) {
            return MetricResult.builder()
                    .name(getMetricName())
                    .score(0)
                    .weight(getWeight())
                    .description("Evaluates commit message quality and conventions")
                    .details("No commits found")
                    .build();
        }

        int goodCommits = 0;
        int totalCommits = commits.size();

        for (CommitInfo commit : commits) {
            String message = commit.getMessage().split("\n")[0];
            if (isGoodCommit(message)) {
                goodCommits++;
            }
        }

        double score = (goodCommits * 100.0) / totalCommits;

        String details = String.format("Analyzed %d commits: %d (%.1f%%) follow conventions",
                totalCommits, goodCommits, score);

        logger.info("Commit quality score for {}/{}: {}", owner, repo, score);

        return MetricResult.builder()
                .name(getMetricName())
                .score(score)
                .weight(getWeight())
                .description("Evaluates commit message quality and conventions")
                .details(details)
                .build();
    }

    /**
     * Determines if a commit message meets quality standards and conventions.
     * <p>
     * Evaluates commit message against multiple quality criteria:
     * <ul>
     *   <li>Not null or empty</li>
     *   <li>Minimum length of 10 characters</li>
     *   <li>Matches conventional commit pattern (optional, gives bonus)</li>
     *   <li>For longer messages (>=20 chars): starts with capital letter, avoids generic terms</li>
     * </ul>
     * <p>
     * Conventional commits that match the pattern {@code GOOD_COMMIT_PATTERN} automatically pass.
     * For other messages, additional quality checks are applied.
     * <p>
     * This method encapsulates quality evaluation logic for unit testing.
     *
     * @param message the commit message to evaluate, may be null
     * @return true if message meets quality standards, false otherwise
     */
    boolean isGoodCommit(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        if (message.length() < 10) {
            return false;
        }

        if (GOOD_COMMIT_PATTERN.matcher(message).matches()) {
            return true;
        }

        String lowerMessage = message.toLowerCase();
        return message.length() >= 20 &&
               Character.isUpperCase(message.charAt(0)) &&
               !lowerMessage.startsWith("merge") &&
               !lowerMessage.startsWith("update") &&
               !lowerMessage.contains("wip");
    }

    /**
     * Returns the human-readable name of this metric.
     * <p>
     * The name identifies this metric as evaluating commit message quality standards.
     *
     * @return string "Commit Quality" as the metric name
     */
    @Override
    public String getMetricName() {
        return "Commit Quality";
    }

    /**
     * Returns the relative importance weight of this commit quality metric.
     * <p>
     * Commit quality contributes 15% to the overall maintainability score,
     * reflecting its importance in assessing development practices.
     *
     * @return weight value 0.15 (15%)
     */
    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
