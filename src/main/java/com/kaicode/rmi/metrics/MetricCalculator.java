package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MetricResult;

import java.io.IOException;

public interface MetricCalculator {
    MetricResult calculate(GitHubClient client, String owner, String repo) throws IOException;
    String getMetricName();
    double getWeight();
}
