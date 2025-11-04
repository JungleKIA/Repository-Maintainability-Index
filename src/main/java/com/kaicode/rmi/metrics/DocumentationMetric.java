package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocumentationMetric implements MetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(DocumentationMetric.class);
    private static final double WEIGHT = 0.20;

    @Override
    public MetricResult calculate(GitHubClient client, String owner, String repo) throws IOException {
        List<String> foundFiles = new ArrayList<>();
        List<String> missingFiles = new ArrayList<>();
        
        String[] documentationFiles = {
            "README.md",
            "CONTRIBUTING.md",
            "LICENSE",
            "CODE_OF_CONDUCT.md",
            "CHANGELOG.md"
        };
        
        int score = 0;
        int maxScore = documentationFiles.length * 20;
        
        for (String file : documentationFiles) {
            if (client.hasFile(owner, repo, file)) {
                foundFiles.add(file);
                score += 20;
            } else {
                missingFiles.add(file);
            }
        }
        
        double normalizedScore = (score * 100.0) / maxScore;
        
        String details = String.format("Found: %s. Missing: %s", 
                foundFiles.isEmpty() ? "none" : String.join(", ", foundFiles),
                missingFiles.isEmpty() ? "none" : String.join(", ", missingFiles));
        
        logger.info("Documentation score for {}/{}: {}", owner, repo, normalizedScore);
        
        return MetricResult.builder()
                .name(getMetricName())
                .score(normalizedScore)
                .weight(getWeight())
                .description("Evaluates the presence of essential documentation files")
                .details(details)
                .build();
    }

    @Override
    public String getMetricName() {
        return "Documentation";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }
}
