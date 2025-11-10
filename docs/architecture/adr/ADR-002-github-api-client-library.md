# ADR-002: Direct GitHub API Client vs Library

## Status

**Accepted** (2024)

## Context

We need to interact with the GitHub API to fetch repository data. There are two main approaches:

1. **Use existing library** (e.g., GitHub API for Java, hub4j/github-api, JCabi GitHub)
2. **Build custom client** using HTTP client (OkHttp)

### Requirements:
- Fetch repository metadata (stars, forks, description)
- Fetch recent commits
- Fetch README content
- Fetch issue statistics
- Fetch branch information
- Handle rate limiting
- Support authentication via token

## Decision

We will **build a custom GitHub API client** using OkHttp and Gson.

### Implementation:
- `GitHubAPIClient` class with focused methods
- OkHttp 4.x for HTTP communication
- Gson for JSON parsing
- Manual rate limit handling
- Lightweight DTOs for API responses

## Alternatives Considered

### 1. GitHub API for Java (com.github-api)
**Pros:**
- Well-maintained library
- Comprehensive API coverage
- Built-in rate limiting
- Type-safe API

**Cons:**
- Heavy dependency (~2MB + transitive dependencies)
- We only need ~5 endpoints
- Abstracts away HTTP layer (harder to customize)
- Forces their domain model
- More classes to load at startup

### 2. Hub4j GitHub API
**Similar pros/cons** to above

### 3. JCabi GitHub
**Cons:**
- Less maintained
- Heavier weight
- Different programming model

## Rationale for Custom Client

1. **Minimal Dependencies**: Only OkHttp + Gson (~1MB total)
2. **Exact Fit**: Only implement what we need (5-6 endpoints)
3. **Control**: Full control over request/response handling
4. **Performance**: No unused code loaded
5. **Transparency**: Clear understanding of all API calls
6. **Flexibility**: Easy to add custom headers, retry logic
7. **Testing**: Simple to mock with MockWebServer

## Consequences

### Positive:
✅ **Lightweight**: Smaller JAR size (~2MB saved)
✅ **Fast startup**: Fewer classes to load
✅ **Simple**: Only code we need, nothing extra
✅ **Maintainable**: Easy to understand and debug
✅ **Flexible**: Easy to customize behavior
✅ **Testable**: Simple mock-based testing

### Negative:
❌ **Manual work**: Need to handle rate limits manually
❌ **API changes**: Need to update if GitHub API changes (rare)
❌ **No abstractions**: Direct JSON parsing (acceptable)
❌ **Limited coverage**: Only endpoints we need (by design)

### Mitigation:
- Document all GitHub API endpoints used
- Add comprehensive tests for API interactions
- Use MockWebServer for offline testing
- Follow GitHub API versioning (use v3 headers)

## Implementation Details

### Core Methods:
```java
public class GitHubAPIClient {
    RepositoryInfo fetchRepositoryInfo(String owner, String repo)
    List<Commit> fetchRecentCommits(String owner, String repo, int count)
    String fetchReadmeContent(String owner, String repo)
    IssueStats fetchIssueStats(String owner, String repo)
    List<String> fetchBranches(String owner, String repo)
}
```

### Rate Limiting:
- Check `X-RateLimit-Remaining` header
- Throw informative exception if limit exceeded
- Suggest using authentication token

### Error Handling:
- 404: Repository not found
- 401/403: Authentication issues
- 429: Rate limit exceeded
- 5xx: GitHub service errors

## When to Reconsider

This decision should be reconsidered if:
- We need >20 GitHub API endpoints
- We need webhook integration
- We need complex GitHub operations (PRs, releases, etc.)
- GitHub API complexity increases significantly

Currently (6 endpoints): Custom client is appropriate
Future (>20 endpoints): Library might be better

## Related Decisions

- [ADR-001: Monolithic CLI Architecture](ADR-001-monolithic-cli-architecture.md)

## References

- [GitHub REST API Documentation](https://docs.github.com/en/rest)
- [OkHttp Documentation](https://square.github.io/okhttp/)
