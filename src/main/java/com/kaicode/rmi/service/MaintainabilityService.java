package com.kaicode.rmi.service;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.metrics.*;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class MaintainabilityService {
    private static final Logger logger = LoggerFactory.getLogger(MaintainabilityService.class);
    
    private final GitHubClient gitHubClient;
    private final List<MetricCalculator> metricCalculators;

    public MaintainabilityService(GitHubClient gitHubClient) {
        this.gitHubClient = Objects.requireNonNull(gitHubClient, "gitHubClient must not be null");
        this.metricCalculators = initializeMetrics();
    }

    public MaintainabilityService(GitHubClient gitHubClient, List<MetricCalculator> metricCalculators) {
        this.gitHubClient = Objects.requireNonNull(gitHubClient, "gitHubClient must not be null");
        this.metricCalculators = Objects.requireNonNull(metricCalculators, "metricCalculators must not be null");
    }

    public MaintainabilityReport analyze(String owner, String repo) throws IOException {
        logger.info("Starting maintainability analysis for {}/{}", owner, repo);
        
        Map<String, MetricResult> metrics = new LinkedHashMap<>();
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        
        for (MetricCalculator calculator : metricCalculators) {
            try {
                MetricResult result = calculator.calculate(gitHubClient, owner, repo);
                metrics.put(result.getName(), result);
                totalWeightedScore += result.getWeightedScore();
                totalWeight += result.getWeight();
                logger.debug("Calculated metric {}: score={}, weight={}", 
                        result.getName(), result.getScore(), result.getWeight());
            } catch (IOException e) {
                logger.error("Failed to calculate metric: {}", calculator.getMetricName(), e);
                throw e;
            }
        }
        
        double overallScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0;
        String recommendation = generateRecommendation(overallScore, metrics);
        
        logger.info("Completed maintainability analysis for {}/{}: score={}", 
                owner, repo, overallScore);
        
        return MaintainabilityReport.builder()
                .repositoryFullName(owner + "/" + repo)
                .overallScore(overallScore)
                .metrics(metrics)
                .recommendation(recommendation)
                .build();
    }

    private List<MetricCalculator> initializeMetrics() {
        List<MetricCalculator> calculators = new ArrayList<>();
        calculators.add(new DocumentationMetric());
        calculators.add(new CommitQualityMetric());
        calculators.add(new ActivityMetric());
        calculators.add(new IssueManagementMetric());
        calculators.add(new CommunityMetric());
        calculators.add(new BranchManagementMetric());
        return calculators;
    }

    String generateRecommendation(double overallScore, Map<String, MetricResult> metrics) {
        StringBuilder recommendation = new StringBuilder();
        
        if (overallScore >= 90) {
            recommendation.append("Excellent repository maintainability! ");
        } else if (overallScore >= 75) {
            recommendation.append("Good repository maintainability. ");
        } else if (overallScore >= 60) {
            recommendation.append("Fair repository maintainability. ");
        } else {
            recommendation.append("Repository maintainability needs improvement. ");
        }
        
        List<String> improvements = new ArrayList<>();
        for (MetricResult metric : metrics.values()) {
            if (metric.getScore() < 60) {
                improvements.add(metric.getName());
            }
        }
        
        if (!improvements.isEmpty()) {
            recommendation.append("Focus on improving: ")
                    .append(String.join(", ", improvements))
                    .append(".");
        } else {
            recommendation.append("Keep up the good work!");
        }
        
        return recommendation.toString();
    }
}
