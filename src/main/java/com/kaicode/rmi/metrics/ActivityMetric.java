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

public class ActivityMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(ActivityMetric.class);
    private static final double WEIGHT = 0.15;
    private static final int RECENT_COMMITS_COUNT = 10;

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

    @Override
    public String getMetricName() {
        return "Activity";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
