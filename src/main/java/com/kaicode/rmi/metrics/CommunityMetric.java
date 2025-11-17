package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;
import com.kaicode.rmi.model.RepositoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Metric calculator that evaluates community engagement and repository popularity.
 * <p>
 * This metric assesses how well a repository attracts and maintains a community by analyzing
 * quantitative indicators of external interest and participation. Higher scores indicate
 * repositories that are popular, actively contributed to, and likely to have strong community support.
 * <p>
 * The metric combines three key community indicators with weighted scoring:
 * <ul>
 *   <li>GitHub stars (40% weight): User interest and bookmarking</li>
 *   <li>Fork count (30% weight): Reuse and derivative work</li>
 *   <li>Contributor count (30% weight): Active developer participation</li>
 * </ul>
 * <p>
 * Scoring thresholds for each indicator:
 * <ul>
 *   <li>Stars: 10+ = 100 points (elite), 5-9 = 50 points, 0-4 = 0-50 points</li>
 *   <li>Forks: 5+ = 100 points (highly forked), 3-4 = 60-80 points, 0-2 = 0-40 points</li>
 *   <li>Contributors: 10+ = 100 points (strong community), 5-9 = 50-90 points, 0-4 = 0-40 points</li>
 * </ul>
 * <p>
 * Final score represents weighted combination of indicators (0-100 scale).
 * Result contributes 15% to overall maintainability score.
 * <p>
 * Implementation analyzes repository metadata to assess community health and adoption.
 *
 * @since 1.0
 * @see MetricCalculator
 * @see GitHubClient#getRepository(String, String)
 * @see GitHubClient#getContributorCount(String, String)
 * @see RepositoryInfo#getStars()
 * @see RepositoryInfo#getForks()
 */
public class CommunityMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(CommunityMetric.class);
    private static final double WEIGHT = 0.15;

    @Override
    public MetricResult calculate(GitHubClient client, String owner, String repo) throws IOException {
        RepositoryInfo repoInfo = client.getRepository(owner, repo);
        
        int stars = repoInfo.getStars();
        int forks = repoInfo.getForks();
        int contributors = client.getContributorCount(owner, repo);
        
        double score = calculateScore(stars, forks, contributors);
        
        String details = String.format("Stars: %d, Forks: %d, Contributors: %d", 
                stars, forks, contributors);
        
        logger.info("Community score for {}/{}: {}", owner, repo, score);
        
        return MetricResult.builder()
                .name(getMetricName())
                .score(score)
                .weight(getWeight())
                .description("Evaluates community engagement and popularity")
                .details(details)
                .build();
    }

    double calculateScore(int stars, int forks, int contributors) {
        double starScore = Math.min(100, stars / 10.0);
        double forkScore = Math.min(100, forks / 5.0);
        double contributorScore = Math.min(100, contributors * 10.0);
        
        return (starScore * 0.4 + forkScore * 0.3 + contributorScore * 0.3);
    }

    @Override
    public String getMetricName() {
        return "Community";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
