package com.kaicode.rmi.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable data model representing GitHub commit information.
 * <p>
 * This class encapsulates data about individual Git commits retrieved from GitHub.
 * All instances are created through the builder pattern to ensure immutability
 * and proper validation, providing foundational data for commit quality analysis.
 * <p>
 * Commit information includes the commit SHA, message content, author details,
 * and timestamp. These are used to evaluate commit message quality, frequency,
 * and contributor patterns in repository maintainability assessments.
 * <p>
 * Instances are thread-safe and can be shared safely across analysis operations.
 * Identity is based on SHA (equals/hashCode considers only SHA for uniqueness).
 * <p>
 * Typical usage:
 * <pre>{@code
 * CommitInfo commit = CommitInfo.builder()
 *     .sha("abc123...")
 *     .message("Fix null pointer exception")
 *     .author("john-doe")
 *     .date(LocalDateTime.now())
 *     .build();
 * }</pre>
 *
 * @since 1.0
 * @see com.kaicode.rmi.github.GitHubClient#getRecentCommits(String, String, int)
 */
public class CommitInfo {
    private final String sha;
    private final String message;
    private final String author;
    private final LocalDateTime date;

    /**
     * Private constructor for creating immutable CommitInfo instances.
     * <p>
     * Called exclusively by {@link Builder#build()} to create validated,
     * immutable commit information objects. All final fields are assigned
     * from validated builder state.
     *
     * @param builder the validated builder containing all field values
     */
    private CommitInfo(Builder builder) {
        this.sha = builder.sha;
        this.message = builder.message;
        this.author = builder.author;
        this.date = builder.date;
    }

    /**
     * Gets the commit SHA (Secure Hash Algorithm) identifier.
     * <p>
     * The SHA is the unique 40-character hexadecimal identifier for this commit.
     * It's the primary key for commit identification and is never null.
     *
     * @return the commit SHA, never null
     */
    public String getSha() {
        return sha;
    }

    /**
     * Gets the commit message text.
     * <p>
     * The full commit message as written by the author, including title
     * and optional body. This is used for commit quality analysis.
     *
     * @return the commit message, may be null if not available
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the name of the commit author.
     * <p>
     * The author's display name as provided in the Git commit metadata.
     * This is used for contributor analysis and community assessment.
     *
     * @return the author name, may be null if not available
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Gets the timestamp when the commit was authored.
     * <p>
     * The date and time when the commit was originally created.
     * This timestamp is used for activity analysis and recentness evaluation.
     *
     * @return the commit timestamp, may be null if not available
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Compares this commit info with another object for equality.
     * <p>
     * Two CommitInfo objects are equal if they have the same commit SHA.
     * SHA is the unique identifier for commits, so other fields are not
     * considered for equality to maintain identity stability.
     *
     * @param o the object to compare with
     * @return true if both objects represent the same commit, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitInfo that = (CommitInfo) o;
        return Objects.equals(sha, that.sha);
    }

    /**
     * Returns a hash code value for this commit info.
     * <p>
     * The hash code is computed from the SHA field only,
     * consistent with the equals method implementation.
     *
     * @return a hash code value for this commit info
     */
    @Override
    public int hashCode() {
        return Objects.hash(sha);
    }

    /**
     * Creates a new Builder instance for constructing CommitInfo objects.
     * <p>
     * This factory method provides the entry point for the builder pattern,
     * allowing fluent construction of immutable CommitInfo instances.
     *
     * @return a new Builder instance for method chaining
     * @since 1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing immutable CommitInfo instances.
     * <p>
     * Provides a fluent API for setting commit information fields
     * before creating the final immutable instance. The builder validates
     * required fields (SHA) during the build process.
     * <p>
     * SHA is required, other fields are optional.
     * Non-required fields will use default values if not explicitly set.
     *
     * @since 1.0
     */
    public static class Builder {
        /**
         * Commit SHA field for the builder.
         */
        private String sha;

        /**
         * Commit message field for the builder.
         */
        private String message;

        /**
         * Author name field for the builder.
         */
        private String author;

        /**
         * Commit timestamp field for the builder.
         */
        private LocalDateTime date;

        /**
         * Sets the commit SHA (required field).
         * <p>
         * The SHA must be a valid 40-character hexadecimal string.
         * This field is required and cannot be null.
         *
         * @param sha the commit SHA, must not be null or empty
         * @return this builder instance for method chaining
         */
        public Builder sha(String sha) {
            this.sha = sha;
            return this;
        }

        /**
         * Sets the commit message text (optional field).
         * <p>
         * The full commit message including title and body.
         * Can be null or empty if not available.
         *
         * @param message the commit message, may be null
         * @return this builder instance for method chaining
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the commit author name (optional field).
         * <p>
         * The display name of the person who authored the commit.
         * Can be null if author information is not available.
         *
         * @param author the author name, may be null
         * @return this builder instance for method chaining
         */
        public Builder author(String author) {
            this.author = author;
            return this;
        }

        /**
         * Sets the commit timestamp (optional field).
         * <p>
         * The date and time when the commit was created.
         * Defaults to null if not specified.
         *
         * @param date the commit timestamp, may be null
         * @return this builder instance for method chaining
         */
        public Builder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        /**
         * Builds and validates a new CommitInfo instance.
         * <p>
         * Creates an immutable CommitInfo from the current builder state.
         * Required fields (SHA) are validated during construction.
         * <p>
         * The returned instance is thread-safe and can be shared safely.
         *
         * @return a new immutable CommitInfo instance
         * @throws NullPointerException if SHA is null
         */
        public CommitInfo build() {
            Objects.requireNonNull(sha, "sha must not be null");
            return new CommitInfo(this);
        }
    }
}
