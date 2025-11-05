package com.kaicode.rmi.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class MaintainabilityReport {
    private final String repositoryFullName;
    private final double overallScore;
    private final Map<String, MetricResult> metrics;
    private final String recommendation;

    private MaintainabilityReport(Builder builder) {
        this.repositoryFullName = builder.repositoryFullName;
        this.overallScore = builder.overallScore;
        this.metrics = Collections.unmodifiableMap(builder.metrics);
        this.recommendation = builder.recommendation;
    }

    public String getRepositoryFullName() {
        return repositoryFullName;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public Map<String, MetricResult> getMetrics() {
        return metrics;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getRating() {
        if (overallScore >= 90) {
            return "EXCELLENT";
        } else if (overallScore >= 75) {
            return "GOOD";
        } else if (overallScore >= 60) {
            return "FAIR";
        } else if (overallScore >= 40) {
            return "POOR";
        } else {
            return "CRITICAL";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaintainabilityReport that = (MaintainabilityReport) o;
        return Double.compare(that.overallScore, overallScore) == 0 &&
                Objects.equals(repositoryFullName, that.repositoryFullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryFullName, overallScore);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String repositoryFullName;
        private double overallScore;
        private Map<String, MetricResult> metrics;
        private String recommendation;

        public Builder repositoryFullName(String repositoryFullName) {
            this.repositoryFullName = repositoryFullName;
            return this;
        }

        public Builder overallScore(double overallScore) {
            this.overallScore = overallScore;
            return this;
        }

        public Builder metrics(Map<String, MetricResult> metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder recommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }

        public MaintainabilityReport build() {
            Objects.requireNonNull(repositoryFullName, "repositoryFullName must not be null");
            Objects.requireNonNull(metrics, "metrics must not be null");
            return new MaintainabilityReport(this);
        }
    }
}
