package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;
import com.kaicode.rmi.model.RepositoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class IssueManagementMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(IssueManagementMetric.class);
    private static final double WEIGHT = 0.20;

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
        int closedIssues = client.getClosedIssuesCount(owner, repo);
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

    @Override
    public String getMetricName() {
        return "Issue Management";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
