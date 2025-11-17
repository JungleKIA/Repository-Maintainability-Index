package com.kaicode.rmi.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable data model representing GitHub repository information.
 * <p>
 * This class encapsulates all metadata retrieved from GitHub's repository API
 * endpoint, providing foundational data for maintainability analysis. All
 * instances are created through the builder pattern to ensure immutability
 * and proper validation.
 * <p>
 * Repository information includes popularity metrics (stars, forks), activity
 * indicators (issues, last updated), configuration flags (wiki, issues), and
 * structural data (default branch, size).
 * <p>
 * Instances are thread-safe and can be shared across multiple analysis operations.
 * Identity is based on owner and name (equals/hashCode consider only these fields).
 * <p>
 * Typical usage:
 * <pre>{@code
 * RepositoryInfo repo = RepositoryInfo.builder()
 *     .owner("octocat")
 *     .name("Hello-World")
 *     .stars(1500)
 *     .forks(500)
 *     .openIssues(42)
 *     .build();
 * }</pre>
 *
 * @since 1.0
 */
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

    /**
     * Private constructor for creating immutable RepositoryInfo instances.
     * <p>
     * Called exclusively by {@link Builder#build()} to create validated,
     * immutable repository information objects. All final fields are assigned
     * from validated builder state.
     *
     * @param builder the validated builder containing all field values
     */
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

    /**
     * Gets the repository owner's username or organization name.
     * <p>
     * This is the first part of the repository's full identifier
     * (owner/name format).
     *
     * @return the repository owner name, never null
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets the repository name.
     * <p>
     * This is the second part of the repository's full identifier
     * (owner/name format).
     *
     * @return the repository name, never null
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the repository description.
     * <p>
     * The description text provided by repository maintainers,
     * or null if no description is set.
     *
     * @return the repository description, may be null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the number of stars (stargazers) for the repository.
     * <p>
     * Stars indicate the popularity and appreciation of the repository.
     * Higher star counts are generally positive for maintainability assessment.
     *
     * @return number of repository stars, may be zero
     */
    public int getStars() {
        return stars;
    }

    /**
     * Gets the number of repository forks.
     * <p>
     * Forks represent the active development community size.
     * More forks indicate broader community contribution potential.
     *
     * @return number of repository forks, may be zero
     */
    public int getForks() {
        return forks;
    }

    /**
     * Gets the count of currently open issues.
     * <p>
     * Open issues reflect repository maintenance needs and activity level.
     * Too many open issues may indicate maintenance burden.
     *
     * @return number of open issues, may be zero
     */
    public int getOpenIssues() {
        return openIssues;
    }

    /**
     * Gets the timestamp when the repository was last updated.
     * <p>
     * This includes any commits, issue updates, or metadata changes.
     * The last update date helps assess recent activity levels.
     *
     * @return last update timestamp, may be null if not available
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Checks if the repository has an enabled wiki.
     * <p>
     * Wiki presence supports community documentation and maintenance
     * efforts. This is a positive factor for project maintainability.
     *
     * @return true if wiki is enabled, false otherwise
     */
    public boolean hasWiki() {
        return hasWiki;
    }

    /**
     * Checks if the repository allows issues to be created.
     * <p>
     * Issue tracking functionality is essential for community
     * interaction and bug/feature request management.
     *
     * @return true if issues are enabled, false otherwise
     */
    public boolean hasIssues() {
        return hasIssues;
    }

    /**
     * Gets the name of the repository's default branch.
     * <p>
     * Typically "main" or "master", this is the primary development branch.
     * Knowing the default branch is important for commit analysis.
     *
     * @return default branch name, may be null if not available
     */
    public String getDefaultBranch() {
        return defaultBranch;
    }

    /**
     * Gets the size of the repository in kilobytes.
     * <p>
     * Repository size affects download times and maintenance effort.
     * Very large repositories may be more challenging to maintain.
     *
     * @return repository size in kilobytes
     */
    public int getSize() {
        return size;
    }

    /**
     * Compares this repository info with another object for equality.
     * <p>
     * Two RepositoryInfo objects are equal if they have the same owner
     * and name. Other fields are not considered for equality to maintain
     * identity stability even if repository metadata changes.
     *
     * @param o the object to compare with
     * @return true if both objects represent the same repository, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepositoryInfo that = (RepositoryInfo) o;
        return Objects.equals(owner, that.owner) && Objects.equals(name, that.name);
    }

    /**
     * Returns a hash code value for this repository info.
     * <p>
     * The hash code is computed from the owner and name fields only,
     * consistent with the equals method implementation.
     *
     * @return a hash code value for this repository info
     */
    @Override
    public int hashCode() {
        return Objects.hash(owner, name);
    }

    /**
     * Creates a new Builder instance for constructing RepositoryInfo objects.
     * <p>
     * This factory method provides the entry point for the builder pattern,
     * allowing fluent construction of immutable RepositoryInfo instances
     * with proper validation.
     *
     * @return a new Builder instance for chaining method calls
     * @since 1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing immutable RepositoryInfo instances.
     * <p>
     * Provides a fluent API for setting all repository information fields
     * before creating the final immutable instance. The builder validates
     * required fields (owner and name) during the build process.
     * <p>
     * All fields are optional except owner and name, which must be non-null.
     * Non-required fields will use default values if not explicitly set.
     *
     * @since 1.0
     */
    public static class Builder {
        /**
         * Repository owner field for the builder.
         */
        private String owner;

        /**
         * Repository name field for the builder.
         */
        private String name;

        /**
         * Repository description field for the builder.
         */
        private String description;

        /**
         * Stars count field for the builder.
         */
        private int stars;

        /**
         * Forks count field for the builder.
         */
        private int forks;

        /**
         * Open issues count field for the builder.
         */
        private int openIssues;

        /**
         * Last updated timestamp field for the builder.
         */
        private LocalDateTime lastUpdated;

        /**
         * Wiki enabled flag for the builder.
         */
        private boolean hasWiki;

        /**
         * Issues enabled flag for the builder.
         */
        private boolean hasIssues;

        /**
         * Default branch name field for the builder.
         */
        private String defaultBranch;

        /**
         * Repository size field for the builder.
         */
        private int size;

        /**
         * Sets the repository owner (required field).
         * <p>
         * The owner must be a valid GitHub username or organization name.
         * This field is required and cannot be null.
         *
         * @param owner the repository owner, must not be null or empty
         * @return this builder instance for method chaining
         */
        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        /**
         * Sets the repository name (required field).
         * <p>
         * The name must be a valid GitHub repository name.
         * This field is required and cannot be null.
         *
         * @param name the repository name, must not be null or empty
         * @return this builder instance for method chaining
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the repository description (optional field).
         * <p>
         * The description text displayed on the repository page.
         * Can be null or empty if no description is available.
         *
         * @param description the repository description, may be null
         * @return this builder instance for method chaining
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the number of repository stars.
         * <p>
         * Indicates the popularity and appreciation of the repository.
         * Defaults to zero if not specified.
         *
         * @param stars the number of stars, must be non-negative
         * @return this builder instance for method chaining
         */
        public Builder stars(int stars) {
            this.stars = stars;
            return this;
        }

        /**
         * Sets the number of repository forks.
         * <p>
         * Represents the active community size and contribution potential.
         * Defaults to zero if not specified.
         *
         * @param forks the number of forks, must be non-negative
         * @return this builder instance for method chaining
         */
        public Builder forks(int forks) {
            this.forks = forks;
            return this;
        }

        /**
         * Sets the number of open issues.
         * <p>
         * Indicates current maintenance load and community activity level.
         * Defaults to zero if not specified.
         *
         * @param openIssues the number of open issues, must be non-negative
         * @return this builder instance for method chaining
         */
        public Builder openIssues(int openIssues) {
            this.openIssues = openIssues;
            return this;
        }

        /**
         * Sets the last update timestamp.
         * <p>
         * The most recent time the repository was modified (commits, issues, etc.).
         * Helps assess recent activity levels.
         *
         * @param lastUpdated the last update timestamp, may be null
         * @return this builder instance for method chaining
         */
        public Builder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        /**
         * Sets whether the repository has wiki enabled.
         * <p>
         * Wiki functionality supports community documentation efforts.
         * Defaults to false if not specified.
         *
         * @param hasWiki true if wiki is enabled, false otherwise
         * @return this builder instance for method chaining
         */
        public Builder hasWiki(boolean hasWiki) {
            this.hasWiki = hasWiki;
            return this;
        }

        /**
         * Sets whether the repository has issues enabled.
         * <p>
         * Issue tracking is essential for community interaction and management.
         * Defaults to false if not specified.
         *
         * @param hasIssues true if issues are enabled, false otherwise
         * @return this builder instance for method chaining
         */
        public Builder hasIssues(boolean hasIssues) {
            this.hasIssues = hasIssues;
            return this;
        }

        /**
         * Sets the default branch name.
         * <p>
         * Typically "main", "master", or custom branch name.
         * Important for commit analysis and repository structure.
         *
         * @param defaultBranch the name of the default branch, may be null
         * @return this builder instance for method chaining
         */
        public Builder defaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
            return this;
        }

        /**
         * Sets the repository size in kilobytes.
         * <p>
         * Repository size affects download times and maintenance effort.
         * Larger repositories may be more challenging to manage.
         *
         * @param size repository size in kilobytes, must be non-negative
         * @return this builder instance for method chaining
         */
        public Builder size(int size) {
            this.size = size;
            return this;
        }

        /**
         * Builds and validates a new RepositoryInfo instance.
         * <p>
         * Creates an immutable RepositoryInfo from the current builder state.
         * Required fields (owner, name) are validated during construction.
         * <p>
         * The returned instance is thread-safe and can be shared safely.
         *
         * @return a new immutable RepositoryInfo instance
         * @throws NullPointerException if owner or name is null
         */
        public RepositoryInfo build() {
            Objects.requireNonNull(owner, "owner must not be null");
            Objects.requireNonNull(name, "name must not be null");
            return new RepositoryInfo(this);
        }
    }
}
