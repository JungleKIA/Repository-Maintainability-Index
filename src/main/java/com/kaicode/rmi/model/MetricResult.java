package com.kaicode.rmi.model;

import java.util.Objects;

public class MetricResult {
    private final String name;
    private final double score;
    private final double weight;
    private final String description;
    private final String details;

    private MetricResult(Builder builder) {
        this.name = builder.name;
        this.score = builder.score;
        this.weight = builder.weight;
        this.description = builder.description;
        this.details = builder.details;
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }

    public double getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }

    public String getDetails() {
        return details;
    }

    public double getWeightedScore() {
        return score * weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricResult that = (MetricResult) o;
        return Double.compare(that.score, score) == 0 &&
                Double.compare(that.weight, weight) == 0 &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, score, weight);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private double score;
        private double weight;
        private String description;
        private String details;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder score(double score) {
            this.score = score;
            return this;
        }

        public Builder weight(double weight) {
            this.weight = weight;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public MetricResult build() {
            Objects.requireNonNull(name, "name must not be null");
            if (score < 0 || score > 100) {
                throw new IllegalArgumentException("score must be between 0 and 100");
            }
            if (weight < 0 || weight > 1) {
                throw new IllegalArgumentException("weight must be between 0 and 1");
            }
            return new MetricResult(this);
        }
    }
}
