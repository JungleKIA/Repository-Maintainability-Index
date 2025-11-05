package com.kaicode.rmi.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class CommitInfo {
    private final String sha;
    private final String message;
    private final String author;
    private final LocalDateTime date;

    private CommitInfo(Builder builder) {
        this.sha = builder.sha;
        this.message = builder.message;
        this.author = builder.author;
        this.date = builder.date;
    }

    public String getSha() {
        return sha;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitInfo that = (CommitInfo) o;
        return Objects.equals(sha, that.sha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sha);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sha;
        private String message;
        private String author;
        private LocalDateTime date;

        public Builder sha(String sha) {
            this.sha = sha;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public CommitInfo build() {
            Objects.requireNonNull(sha, "sha must not be null");
            return new CommitInfo(this);
        }
    }
}
