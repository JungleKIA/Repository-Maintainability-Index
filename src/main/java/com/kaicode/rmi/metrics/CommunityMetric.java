package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;
import com.kaicode.rmi.model.RepositoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
