package com.kaicode.rmi.service;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.metrics.*;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Core service for performing maintainability analysis on GitHub repositories.
 * <p>
 * This service coordinates the analysis of GitHub repositories by applying
 * multiple maintainability metrics and combining their results into a
 * comprehensive maintainability report. It uses parallel metric calculation
 * with proper error handling and weighted scoring.
 * <p>
 * The service supports:
 * <ul>
 *   <li>Standard metric calculation (documentation, commit quality, activity, etc.)</li>
 *   <li>Weighted scoring system for importance-based metrics</li>
 *   <li>Comprehensive error handling with detailed logging</li>
 *   <li>Thread-safe operation</li>
 * </ul>
 * <p>
 * All methods are thread-safe and can be called concurrently. Internal state
 * is immutable after construction.
 *
 * @since 1.0
 * @see GitHubClient
 * @see MetricCalculator
 * @see MaintainabilityReport
 */
public class MaintainabilityService {
    /**
     * Logger for maintainability analysis operations and debugging information.
     * <p>
     * Logs analysis progress, metric calculation results, and error conditions.
     * Uses SLF4J logging framework for consistent logging across the application.
     */
    private static final Logger logger = LoggerFactory.getLogger(MaintainabilityService.class);

    /**
     * GitHub client for accessing repository data and API operations.
     * <p>
     * Used to fetch repository information, commits, issues, and other data
     * needed for maintainability calculations. Never null after construction.
     * <p>
     * Thread-safe and should support concurrent API calls.
     */
    private final GitHubClient gitHubClient;

    /**
     * List of metric calculators to apply during analysis.
     * <p>
     * Contains instances of all supported maintainability metrics:
     * documentation, commit quality, activity, issue management, community,
     * and branch management. Order determines calculation sequence.
     * <p>
     * Never null or empty after construction.
     */
    private final List<MetricCalculator> metricCalculators;

    /**
     * Creates a new maintainability service with standard metrics.
     * <p>
     * Initializes the service with the provided GitHub client and default
     * set of maintainability metrics (documentation, commit quality, activity,
     * issue management, community, and branch management).
     *
     * @param gitHubClient the GitHub client for API operations,
     *                     must not be null
     * @throws NullPointerException if gitHubClient is null
     * @since 1.0
     * @see GitHubClient
     * @see #initializeMetrics()
     */
    public MaintainabilityService(GitHubClient gitHubClient) {
        this.gitHubClient = Objects.requireNonNull(gitHubClient, "gitHubClient must not be null");
        this.metricCalculators = initializeMetrics();
    }

    /**
     * Creates a new maintainability service with custom metrics.
     * <p>
     * Initializes the service with the provided GitHub client and custom
     * list of metric calculators. Useful for testing or specialized analysis
     * configurations.
     *
     * @param gitHubClient the GitHub client for API operations,
     *                     must not be null
     * @param metricCalculators list of metric calculators to use,
     *                          must not be null or empty
     * @throws NullPointerException if gitHubClient or metricCalculators is null
     * @since 1.0
     * @see GitHubClient
     * @see MetricCalculator
     */
    public MaintainabilityService(GitHubClient gitHubClient, List<MetricCalculator> metricCalculators) {
        this.gitHubClient = Objects.requireNonNull(gitHubClient, "gitHubClient must not be null");
        this.metricCalculators = Objects.requireNonNull(metricCalculators, "metricCalculators must not be null");
    }

    /**
     * Performs comprehensive maintainability analysis of a GitHub repository.
     * <p>
     * This is the main business method that orchestrates the entire analysis
     * workflow. It applies all configured metrics to calculate maintainability
     * scores, handles errors gracefully, and generates actionable recommendations.
     * <p>
     * The analysis process:
     * <ol>
     *   <li>Validates repository access</li>
     *   <li>Applies each metric calculator in sequence</li>
     *   <li>Calculates weighted overall score</li>
     *   <li>Generates improvement recommendations</li>
     *   <li>Logs progress and results</li>
     * </ol>
     * <p>
     * This method is thread-safe if the GitHub client is thread-safe.
     *
     * @param owner the GitHub username or organization name,
     *              must not be null or empty
     * @param repo the repository name, must not be null or empty
     * @return comprehensive maintainability report with scores,
     *         metrics, and recommendations, never null
     * @throws NullPointerException if owner or repo is null
     * @throws IllegalArgumentException if owner or repo is empty
     * @throws IOException if repository access fails or API limits exceeded
     * @since 1.0
     * @see MetricCalculator#calculate(GitHubClient, String, String)
     * @see MaintainabilityReport
     */
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

    /**
     * Initializes the default set of maintainability metrics.
     * <p>
     * Creates and returns a list of all standard metric calculators
     * in the recommended execution order. These metrics cover:
     * <ul>
     *   <li>Documentation quality</li>
     *   <li>Commit message quality</li>
     *   <li>Development activity levels</li>
     *   <li>Issue management effectiveness</li>
     *   <li>Community engagement</li>
     *   <li>Branch management practices</li>
     * </ul>
     * <p>
     * Order is important for dependency resolution and user experience.
     *
     * @return immutable list of metric calculators, never null or empty
     */
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

    /**
     * Generates a human-readable recommendation based on analysis results.
     * <p>
     * Creates a comprehensive recommendation message that includes:
     * <ul>
     *   <li>Overall assessment (Excellent/Good/Fair/Needs improvement)</li>
     *   <li>Specific areas that need improvement (if any)</li>
     *   <li>Encouragement for well-performing repositories</li>
     * </ul>
     * <p>
     * The recommendation is derived from the overall score threshold levels
     * and identifies metrics scoring below 60% for targeted improvements.
     *
     * @param overallScore the weighted overall maintainability score (0-100)
     * @param metrics the map of calculated metrics by name, must not be null
     * @return formatted recommendation message, never null or empty
     */
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
