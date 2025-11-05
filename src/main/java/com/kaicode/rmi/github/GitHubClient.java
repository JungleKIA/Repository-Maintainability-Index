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

public class GitHubClient {
    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);
    private static final String DEFAULT_API_BASE_URL = "https://api.github.com";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String token;
    private final String apiBaseUrl;

    public GitHubClient(String token) {
        this(token, DEFAULT_API_BASE_URL);
    }

    public GitHubClient(String token, String apiBaseUrl) {
        this.token = token;
        this.apiBaseUrl = apiBaseUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public GitHubClient(OkHttpClient httpClient, String token, String apiBaseUrl) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.token = token;
        this.apiBaseUrl = apiBaseUrl;
        this.gson = new Gson();
    }

    public RepositoryInfo getRepository(String owner, String repo) throws IOException {
        String url = String.format("%s/repos/%s/%s", apiBaseUrl, owner, repo);
        String responseBody = executeRequest(url);
        
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        
        return RepositoryInfo.builder()
                .owner(owner)
                .name(repo)
                .description(json.has("description") && !json.get("description").isJsonNull() 
                        ? json.get("description").getAsString() : "")
                .stars(json.get("stargazers_count").getAsInt())
                .forks(json.get("forks_count").getAsInt())
                .openIssues(json.get("open_issues_count").getAsInt())
                .lastUpdated(parseDateTime(json.get("updated_at").getAsString()))
                .hasWiki(json.get("has_wiki").getAsBoolean())
                .hasIssues(json.get("has_issues").getAsBoolean())
                .defaultBranch(json.get("default_branch").getAsString())
                .size(json.get("size").getAsInt())
                .build();
    }

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

    public int getClosedIssuesCount(String owner, String repo) throws IOException {
        String url = String.format("%s/repos/%s/%s/issues?state=closed&per_page=1", 
                apiBaseUrl, owner, repo);
        Response response = executeRequestWithResponse(url);
        
        String linkHeader = response.header("Link");
        if (linkHeader != null && linkHeader.contains("last")) {
            String lastPage = extractLastPage(linkHeader);
            if (lastPage != null) {
                return Integer.parseInt(lastPage);
            }
        }
        
        String responseBody = response.body().string();
        JsonArray issues = gson.fromJson(responseBody, JsonArray.class);
        return issues.size();
    }

    public int getBranchCount(String owner, String repo) throws IOException {
        String url = String.format("%s/repos/%s/%s/branches?per_page=100", 
                apiBaseUrl, owner, repo);
        String responseBody = executeRequest(url);
        JsonArray branches = gson.fromJson(responseBody, JsonArray.class);
        return branches.size();
    }

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
