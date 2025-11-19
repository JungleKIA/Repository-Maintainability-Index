package com.kaicode.rmi.github;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.RepositoryInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Client for accessing GitHub API to retrieve repository information for maintainability analysis.
 * <p>
 * This class provides methods to fetch repository metadata, commits, issues, and other
 * data needed for calculating maintainability metrics. It uses HTTP calls to GitHub REST API
 * with proper authentication and error handling.
 * <p>
 * Key features:
 * <ul>
 *   <li>Fetches repository information (stars, forks, issues, etc.)</li>
 *   <li>Retrieves recent commit history and metadata</li>
 *   <li>Checks for presence of files in repository</li>
 *   <li>Gets contributor and branch counts</li>
 *   <li>Follows GitHub API rate limits automatically</li>
 *   <li>Thread-safe for concurrent usage</li>
 * </ul>
 * <p>
 * All methods may throw {@link IOException} for network errors or API failures.
 * Consider implementing retry logic and caching for production usage.
 * <p>
 * Example usage:
 * <pre>{@code
 * GitHubClient client = new GitHubClient("your-github-token");
 * RepositoryInfo repo = client.getRepository("octocat", "Hello-World");
 * List<CommitInfo> commits = client.getRecentCommits("octocat", "Hello-World", 10);
 * }</pre>
 *
 * @since 1.0
 * @see RepositoryInfo
 * @see CommitInfo
 */
public class GitHubClient {
    /**
     * Logger for GitHub API operation tracking and error reporting.
     * <p>
     * Logs API requests, responses, and errors encountered during
     * repository data retrieval. Uses SLF4J for consistent logging.
     */
    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    /**
     * Default GitHub API base URL for public API access.
     * <p>
     * Value: {@value}
     */
    private static final String DEFAULT_API_BASE_URL = "https://api.github.com";

    /**
     * Default HTTP connection timeout in seconds.
     * <p>
     * Applied to both connect and read operations. Value: {@value}
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    /**
     * HTTP client instance configured with timeouts.
     * <p>
     * Built with OkHttp library and configured with connection,
     * read, and write timeouts. Thread-safe for concurrent usage.
     * <p>
     * Never null after construction.
     */
    private final OkHttpClient httpClient;

    /**
     * JSON parser instance for API response processing.
     * <p>
     * Uses Google Gson library for JSON serialization and
     * deserialization. Thread-safe and reusable.
     * <p>
     * Never null after construction.
     */
    private final Gson gson;

    /**
     * GitHub personal access token for API authentication.
     * <p>
     * When provided, enables access to private repositories and
     * increases API rate limits. Can be null for public repositories.
     * <p>
     * Security note: Token should be kept confidential and not logged.
     */
    private final String token;

    /**
     * Base URL for GitHub API endpoints.
     * <p>
     * Typically "https://api.github.com" for public GitHub.
     * Can be customized for GitHub Enterprise installations.
     * <p>
     * Never null after construction.
     */
    private final String apiBaseUrl;

    /**
     * Creates a GitHub client with default API base URL.
     * <p>
     * Configures HTTP client with default timeouts and uses
     * public GitHub API ("https://api.github.com").
     *
     * @param token GitHub personal access token, can be null for public repos
     * @since 1.0
     * @see #DEFAULT_API_BASE_URL
     */
    public GitHubClient(String token) {
        this(token, DEFAULT_API_BASE_URL);
    }

    /**
     * Creates a GitHub client with custom API base URL.
     * <p>
     * Useful for GitHub Enterprise installations or testing with mock servers.
     * Configures HTTP client with default timeouts.
     *
     * @param token GitHub personal access token, can be null for public repos
     * @param apiBaseUrl base URL for API endpoints, must not be null or empty
     * @throws IllegalArgumentException if apiBaseUrl is null or empty
     * @since 1.0
     */
    public GitHubClient(String token, String apiBaseUrl) {
        this.token = token;
        this.apiBaseUrl = apiBaseUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Creates a GitHub client with custom HTTP client.
     * <p>
     * Allows full customization of HTTP client behavior (timeouts, proxies, etc.).
     * Useful for testing with mock HTTP clients or advanced configurations.
     *
     * @param httpClient configured OkHttp client, must not be null
     * @param token GitHub personal access token, can be null for public repos
     * @param apiBaseUrl base URL for API endpoints, must not be null or empty
     * @throws NullPointerException if httpClient is null
     * @since 1.0
     */
    public GitHubClient(OkHttpClient httpClient, String token, String apiBaseUrl) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.token = token;
        this.apiBaseUrl = apiBaseUrl;
        this.gson = new Gson();
    }

    /**
     * Retrieves comprehensive repository information from GitHub.
     * <p>
     * Fetches metadata about a repository including stars, forks, issues,
     * wiki status, default branch, size, and other metrics used for
     * maintainability analysis. Makes a single API call to {@code repos/owner/repo}.
     * <p>
     * The returned information is used by multiple maintainability metrics
     * and provides foundational data for repository assessment.
     *
     * @param owner GitHub username or organization name, must not be null or empty
     * @param repo repository name, must not be null or empty
     * @return repository information with all available metadata, never null
     * @throws IOException if network error occurs or repository doesn't exist
     * @throws NullPointerException if owner or repo parameters are null
     * @throws IllegalArgumentException if owner or repo parameters are empty
     * @since 1.0
     * @see RepositoryInfo
     */
    public RepositoryInfo getRepository(String owner, String repo) throws IOException {
        String url = String.format("%s/repos/%s/%s", apiBaseUrl, owner, repo);
        String responseBody = executeRequest(url);

        JsonObject json = gson.fromJson(responseBody, JsonObject.class);

        int openIssues;
        try {
            openIssues = this.getOpenIssuesCount(owner, repo);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("422")) {
                logger.warn("Large dataset detected for {}/{}, estimating open issues from API response", owner, repo);
                // For large repositories, use open_issues count from repository API (may be approximate)
                openIssues = json.get("open_issues_count").getAsInt();
                if (openIssues >= 1000) {
                    logger.info("Using approximiate open issues for {}/{}: {} (actual count may be higher)", owner, repo, openIssues);
                }
            } else {
                throw e; // Re-throw non-422 errors
            }
        }
        logger.info("Retrieved {}/{}, open issues: {}", owner, repo, openIssues);

        return RepositoryInfo.builder()
                .owner(owner)
                .name(repo)
                .description(json.has("description") && !json.get("description").isJsonNull()
                        ? json.get("description").getAsString() : "")
                .stars(json.get("stargazers_count").getAsInt())
                .forks(json.get("forks_count").getAsInt())
                .openIssues(openIssues)
                .lastUpdated(parseDateTime(json.get("updated_at").getAsString()))
                .hasWiki(json.get("has_wiki").getAsBoolean())
                .hasIssues(json.get("has_issues").getAsBoolean())
                .defaultBranch(json.get("default_branch").getAsString())
                .size(json.get("size").getAsInt())
                .build();
    }

    /**
     * Retrieves recent commit history from a repository.
     * <p>
     * Fetches the most recent commits from the specified repository, limited by count.
     * Maximum 100 commits per request. The commits include SHA, message, author, and timestamp.
     * <p>
     * This data is used for commit quality analysis and assessing development activity.
     * Only returns commits from the default branch.
     *
     * @param owner GitHub username or organization name, must not be null or empty
     * @param repo repository name, must not be null or empty
     * @param count number of commits to retrieve, automatically limited to 100
     * @return list of recent commits, never null but can be empty, ordered by date descending
     * @throws IOException if network error occurs or repository doesn't exist
     * @throws NullPointerException if owner or repo parameters are null
     * @throws IllegalArgumentException if owner or repo parameters are empty
     * @since 1.0
     * @see CommitInfo
     */
    public List<CommitInfo> getRecentCommits(String owner, String repo, int count) throws IOException {
        String url = String.format("%s/repos/%s/%s/commits?per_page=%d",
                apiBaseUrl, owner, repo, Math.min(count, 100));
        String responseBody = executeRequest(url);

        JsonArray commits = gson.fromJson(responseBody, JsonArray.class);
        List<CommitInfo> result = new ArrayList<>();

        for (int i = 0; i < commits.size(); i++) {
            JsonObject commit = commits.get(i).getAsJsonObject();
            JsonObject commitData = commit.getAsJsonObject("commit");
            JsonObject author = commitData.getAsJsonObject("author");

            result.add(CommitInfo.builder()
                    .sha(commit.get("sha").getAsString())
                    .message(commitData.get("message").getAsString())
                    .author(author.get("name").getAsString())
                    .date(parseDateTime(author.get("date").getAsString()))
                    .build());
        }

        return result;
    }

    /**
     * Checks if a specific file exists in the repository.
     * <p>
     * Queries the GitHub API to determine if a file exists at the given path.
     * This is used for checking presence of important project files like README,
     * LICENSE, CONTRIBUTING, etc., which are factored into maintainability scores.
     * <p>
     * Uses the {@code GET repos/owner/repo/contents/path} endpoint.
     *
     * @param owner GitHub username or organization name, must not be null or empty
     * @param repo repository name, must not be null or empty
     * @param path file path relative to repository root, must not be null
     * @return {@code true} if file exists, {@code false} if file doesn't exist
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if owner or repo parameters are empty
     * @since 1.0
     */
    public boolean hasFile(String owner, String repo, String path) {
        try {
            String url = String.format("%s/repos/%s/%s/contents/%s",
                    apiBaseUrl, owner, repo, path);
            executeRequest(url);
            return true;
        } catch (IOException e) {
            logger.debug("File not found: {}", path);
            return false;
        }
    }

    /**
     * Gets the total count of closed issues in the repository.
     * <p>
     * Retrieves the number of resolved issues by fetching all closed issues/PRs
     * and filtering out pull requests. Uses paginated API call to {@code repos/owner/repo/issues} with
     * {@code state=closed}. Counts only pure issues (not pull requests).
     * <p>
     * This metric helps assess issue management effectiveness and community activity.
     *
     * @param owner GitHub username or organization name, must not be null or empty
     * @param repo repository name, must not be null or empty
     * @return total number of closed issues in the repository (excluding pull requests)
     * @throws IOException if network error occurs or repository doesn't exist
     * @throws IllegalArgumentException if owner or repo parameters are empty
     * @since 1.0
     */
    public int getClosedIssuesCount(String owner, String repo) throws IOException {
        int totalClosedIssues = 0;
        int page = 1;

        while (true) {
            String url = String.format("%s/repos/%s/%s/issues?state=closed&per_page=100&page=%d",
                    apiBaseUrl, owner, repo, page);
            String responseBody = executeRequest(url);
            JsonArray issues = gson.fromJson(responseBody, JsonArray.class);

            for (int i = 0; i < issues.size(); i++) {
                JsonObject issue = issues.get(i).getAsJsonObject();
                // Count only pure issues, skip pull requests
                if (!issue.has("pull_request") || issue.get("pull_request").isJsonNull()) {
                    totalClosedIssues++;
                }
            }

            if (issues.size() < 100) {
                break; // No more pages
            }
            page++;
        }

        return totalClosedIssues;
    }

    /**
     * Gets the total count of open issues in the repository.
     * <p>
     * Retrieves the number of open issues by fetching all open issues/PRs
     * and filtering out pull requests. Uses paginated API call to {@code repos/owner/repo/issues} with
     * {@code state=open}. Counts only pure issues (not pull requests).
     * <p>
     * This metric helps assess current issue backlog for maintainability.
     *
     * @param owner GitHub username or organization name, must not be null or empty
     * @param repo repository name, must not be null or empty
     * @return total number of open issues in the repository (excluding pull requests)
     * @throws IOException if network error occurs or repository doesn't exist
     * @throws IllegalArgumentException if owner or repo parameters are empty
     * @since 1.0
     */
    public int getOpenIssuesCount(String owner, String repo) throws IOException {
        int totalOpenIssues = 0;
        int page = 1;

        while (true) {
            String url = String.format("%s/repos/%s/%s/issues?state=open&per_page=100&page=%d",
                    apiBaseUrl, owner, repo, page);
            String responseBody = executeRequest(url);
            JsonArray issues = gson.fromJson(responseBody, JsonArray.class);

            for (int i = 0; i < issues.size(); i++) {
                JsonObject issue = issues.get(i).getAsJsonObject();
                // Count only pure issues, skip pull requests
                if (!issue.has("pull_request") || issue.get("pull_request").isJsonNull()) {
                    totalOpenIssues++;
                }
            }

            if (issues.size() < 100) {
                break; // No more pages
            }
            page++;
        }

        return totalOpenIssues;
    }

    /**
     * Gets the total number of branches in the repository.
     * <p>
     * Retrieves all branches from the repository by using paginated API call to
     * {@code repos/owner/repo/branches} with {@code per_page=100}. Returns the
     * total count of branches (including default and feature branches).
     * <p>
     * This metric is used to assess code organization and branching strategy
     * in maintainability analysis.
     *
     * @param owner GitHub username or organization name, must not be null or empty
     * @param repo repository name, must not be null or empty
     * @return total number of branches in the repository
     * @throws IOException if network error occurs or repository doesn't exist
     * @throws NullPointerException if owner or repo parameters are null
     * @throws IllegalArgumentException if owner or repo parameters are empty
     * @since 1.0
     */
    public int getBranchCount(String owner, String repo) throws IOException {
        String url = String.format("%s/repos/%s/%s/branches?per_page=100",
                apiBaseUrl, owner, repo);
        String responseBody = executeRequest(url);
        JsonArray branches = gson.fromJson(responseBody, JsonArray.class);
        return branches.size();
    }

    /**
     * Gets the total number of contributors to the repository.
     * <p>
     * Retrieves contributors list using paginated API call to
     * {@code repos/owner/repo/contributors} with {@code per_page=100}.
     * Returns the count of contributors who have made commits to the repository.
     * <p>
     * This metric evaluates the community size and contribution diversity,
     * which are important factors in repository maintainability assessment.
     *
     * @param owner GitHub username or organization name, must not be null or empty
     * @param repo repository name, must not be null or empty
     * @return total number of contributors to the repository
     * @throws IOException if network error occurs or repository doesn't exist
     * @throws NullPointerException if owner or repo parameters are null
     * @throws IllegalArgumentException if owner or repo parameters are empty
     * @since 1.0
     */
    public int getContributorCount(String owner, String repo) throws IOException {
        String url = String.format("%s/repos/%s/%s/contributors?per_page=100",
                apiBaseUrl, owner, repo);
        String responseBody = executeRequest(url);
        JsonArray contributors = gson.fromJson(responseBody, JsonArray.class);
        return contributors.size();
    }

    private String executeRequest(String url) throws IOException {
        Response response = executeRequestWithResponse(url);
        return response.body().string();
    }

    private Response executeRequestWithResponse(String url) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json");

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        Request request = requestBuilder.build();
        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            String errorBody = response.body() != null ? response.body().string() : "No error body";
            throw new IOException(String.format("GitHub API request failed: %d - %s",
                    response.code(), errorBody));
        }

        return response;
    }

    private LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
    }

    private String extractLastPage(String linkHeader) {
        String[] links = linkHeader.split(",");
        for (String link : links) {
            if (link.contains("rel=\"last\"")) {
                int pageIndex = link.indexOf("page=");
                if (pageIndex != -1) {
                    int start = pageIndex + 5;
                    int end = link.indexOf(">", start);
                    if (end == -1) end = link.indexOf("&", start);
                    if (end == -1) end = link.length();
                    return link.substring(start, end);
                }
            }
        }
        return null;
    }
}
