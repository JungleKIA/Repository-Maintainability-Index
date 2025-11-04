package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.MetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class CommitQualityMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(CommitQualityMetric.class);
    private static final double WEIGHT = 0.15;
    private static final int COMMITS_TO_ANALYZE = 50;
    private static final Pattern GOOD_COMMIT_PATTERN = Pattern.compile(
            "^(feat|fix|docs|style|refactor|test|chore|perf|ci|build)(\\(.+\\))?:.+", 
            Pattern.CASE_INSENSITIVE);

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

    @Override
    public String getMetricName() {
        return "Commit Quality";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
