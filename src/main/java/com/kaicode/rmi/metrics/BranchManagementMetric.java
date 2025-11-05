package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BranchManagementMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(BranchManagementMetric.class);
    private static final double WEIGHT = 0.15;

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

    @Override
    public String getMetricName() {
        return "Branch Management";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
