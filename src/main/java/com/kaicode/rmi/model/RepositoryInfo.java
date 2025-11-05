package com.kaicode.rmi.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class RepositoryInfo {
    private final String owner;
    private final String name;
    private final String description;
    private final int stars;
    private final int forks;
    private final int openIssues;
    private final LocalDateTime lastUpdated;
    private final boolean hasWiki;
    private final boolean hasIssues;
    private final String defaultBranch;
    private final int size;

    private RepositoryInfo(Builder builder) {
        this.owner = builder.owner;
        this.name = builder.name;
        this.description = builder.description;
        this.stars = builder.stars;
        this.forks = builder.forks;
        this.openIssues = builder.openIssues;
        this.lastUpdated = builder.lastUpdated;
        this.hasWiki = builder.hasWiki;
        this.hasIssues = builder.hasIssues;
        this.defaultBranch = builder.defaultBranch;
        this.size = builder.size;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getStars() {
        return stars;
    }

    public int getForks() {
        return forks;
    }

    public int getOpenIssues() {
        return openIssues;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public boolean hasWiki() {
        return hasWiki;
    }

    public boolean hasIssues() {
        return hasIssues;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepositoryInfo that = (RepositoryInfo) o;
        return Objects.equals(owner, that.owner) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String owner;
        private String name;
        private String description;
        private int stars;
        private int forks;
        private int openIssues;
        private LocalDateTime lastUpdated;
        private boolean hasWiki;
        private boolean hasIssues;
        private String defaultBranch;
        private int size;

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder stars(int stars) {
            this.stars = stars;
            return this;
        }

        public Builder forks(int forks) {
            this.forks = forks;
            return this;
        }

        public Builder openIssues(int openIssues) {
            this.openIssues = openIssues;
            return this;
        }

        public Builder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder hasWiki(boolean hasWiki) {
            this.hasWiki = hasWiki;
            return this;
        }

        public Builder hasIssues(boolean hasIssues) {
            this.hasIssues = hasIssues;
            return this;
        }

        public Builder defaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public RepositoryInfo build() {
            Objects.requireNonNull(owner, "owner must not be null");
            Objects.requireNonNull(name, "name must not be null");
            return new RepositoryInfo(this);
        }
    }
}
