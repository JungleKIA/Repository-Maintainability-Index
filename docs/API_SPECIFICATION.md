# API Specification: Repository Maintainability Index

**Version**: 1.0.0  
**Last Updated**: January 17, 2025  
**Type**: Command-Line Interface (CLI)  
**Format**: Human-readable text and JSON output

---

## Table of Contents

1. [Overview](#overview)
2. [CLI Interface](#cli-interface)
3. [Input Specifications](#input-specifications)
4. [Output Specifications](#output-specifications)
5. [Exit Codes](#exit-codes)
6. [Environment Variables](#environment-variables)
7. [External API Dependencies](#external-api-dependencies)
8. [Error Handling](#error-handling)
9. [Examples](#examples)

---

## Overview

Repository Maintainability Index (RMI) provides a **command-line interface** for analyzing GitHub repositories. It does **not** expose a REST API or web service interface - it is a pure CLI tool designed for terminal/shell usage.

### Interface Type

```
┌────────────────────────────────────────┐
│         User Terminal                  │
└─────────────┬──────────────────────────┘
              │ Shell commands
              ▼
┌────────────────────────────────────────┐
│      RMI CLI Application               │
│  (Java JAR executable)                 │
└──────────────┬──────────────────────────┘
               │ HTTP/REST
               ▼
┌────────────────────────────────────────┐
│  External APIs (GitHub, OpenRouter)    │
└────────────────────────────────────────┘
```

---

## CLI Interface

### Command Structure

```bash
java -jar repo-maintainability-index-1.0.0.jar [COMMAND] [OPTIONS] [ARGUMENTS]
```

### Available Commands

#### 1. Root Command (Help)

**Usage**:
```bash
rmi [--help] [--version]
```

**Options**:
- `--help`, `-h`: Display help information
- `--version`, `-V`: Display version information

**Example**:
```bash
$ rmi --help
Usage: rmi [-hV] [COMMAND]
Repository Maintainability Index - Automated GitHub repository quality assessment
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  analyze  Analyze a GitHub repository for maintainability metrics
```

#### 2. Analyze Command

**Usage**:
```bash
rmi analyze OWNER/REPO [OPTIONS]
```

**Positional Arguments**:
- `OWNER/REPO` (required): GitHub repository in format `owner/repository`
  - Examples: `facebook/react`, `microsoft/vscode`, `apache/kafka`

**Options**:

| Option | Short | Type | Default | Description |
|--------|-------|------|---------|-------------|
| `--token` | `-t` | String | None | GitHub personal access token |
| `--format` | `-f` | String | `text` | Output format: `text` or `json` |
| `--llm` | - | Flag | false | Enable LLM-powered analysis |
| `--model` | `-m` | String | `openai/gpt-oss-20b:free` | LLM model to use |
| `--help` | `-h` | Flag | - | Show help for analyze command |

**Example**:
```bash
$ rmi analyze facebook/react --token ghp_xxx --format json --llm
```

---

## Input Specifications

### Repository Name Format

**Specification**:
```
OWNER/REPO

OWNER: [a-zA-Z0-9_.-]+
REPO:  [a-zA-Z0-9_.-]+
```

**Valid Examples**:
- `facebook/react`
- `kubernetes/kubernetes`
- `vercel/next.js`
- `spring-projects/spring-boot`

**Invalid Examples**:
- `facebook` (missing repo)
- `facebook/` (empty repo name)
- `facebook/react/extra` (too many parts)
- `facebook react` (space instead of slash)

### GitHub Token Format

**Specification**:
- Classic PAT: `ghp_[A-Za-z0-9]{36}`
- Fine-grained PAT: `github_pat_[A-Za-z0-9_]{82}`

**Required Scopes**:
- `public_repo`: Access public repositories
- `repo:status`: Access commit status

**Optional Scopes** (for private repos):
- `repo`: Full repository access

### LLM Model Format

**Specification**:
- Format: `provider/model-name[:variant]`
- Examples:
  - `openai/gpt-3.5-turbo`
  - `openai/gpt-4`
  - `anthropic/claude-3-haiku`
  - `openai/gpt-oss-20b:free`

**Supported Providers**:
- OpenAI: `openai/*`
- Anthropic: `anthropic/*`
- Google: `google/*`
- Meta: `meta/*`

---

## Output Specifications

### Text Format (Default)

**Structure**:
```
═══════════════════════════════════════════════════════════════
  Repository Maintainability Index Report
═══════════════════════════════════════════════════════════════

Repository: <owner>/<repo>
Overall Score: <score>/100
Rating: <EXCELLENT|GOOD|FAIR|POOR>

───────────────────────────────────────────────────────────────
  Detailed Metrics
───────────────────────────────────────────────────────────────

▪ <Metric Name>: <score>/100 (weight: <weight>%)
  <Description>
  Details: <metric-specific details>

[... repeated for each metric ...]

───────────────────────────────────────────────────────────────
  Recommendation
───────────────────────────────────────────────────────────────

<Recommendation text>

═══════════════════════════════════════════════════════════════
```

**Example**:
```
═══════════════════════════════════════════════════════════════
  Repository Maintainability Index Report
═══════════════════════════════════════════════════════════════

Repository: facebook/react
Overall Score: 87.50/100
Rating: GOOD

───────────────────────────────────────────────────────────────
  Detailed Metrics
───────────────────────────────────────────────────────────────

▪ Documentation: 100.00/100 (weight: 20%)
  Evaluates the presence of essential documentation files
  Details: Found: README.md, CONTRIBUTING.md, LICENSE, CODE_OF_CONDUCT.md, CHANGELOG.md. Missing: none

▪ Issue Management: 95.00/100 (weight: 20%)
  Evaluates issue closure rate and responsiveness
  Details: Closure rate: 95.0%, Open issues: 450

───────────────────────────────────────────────────────────────
  Recommendation
───────────────────────────────────────────────────────────────

Good repository maintainability. Keep up the good work!

═══════════════════════════════════════════════════════════════
```

### JSON Format

**Schema**:
```json
{
  "repositoryFullName": "string",
  "overallScore": "number (0-100)",
  "rating": "string (EXCELLENT|GOOD|FAIR|POOR)",
  "metrics": {
    "<metric-name>": {
      "name": "string",
      "score": "number (0-100)",
      "weight": "number (0-100)",
      "weightedScore": "number (0-100)",
      "description": "string",
      "details": "string"
    }
  },
  "recommendation": "string",
  "llmAnalysis": {
    "readmeAnalysis": {
      "clarityScore": "number (0-100)",
      "completenessScore": "number (0-100)",
      "newcomerFriendlinessScore": "number (0-100)",
      "strengths": ["string"],
      "improvements": ["string"]
    },
    "commitQualityAnalysis": {
      "consistencyScore": "number (0-100)",
      "messageQualityScore": "number (0-100)",
      "conventionAdherenceScore": "number (0-100)",
      "observations": ["string"],
      "recommendations": ["string"]
    },
    "communityHealthAnalysis": {
      "responsivenessScore": "number (0-100)",
      "helpfulnessScore": "number (0-100)",
      "toneScore": "number (0-100)",
      "positiveAspects": ["string"],
      "concerningPatterns": ["string"]
    },
    "recommendations": [
      {
        "title": "string",
        "description": "string",
        "priority": "string (HIGH|MEDIUM|LOW)",
        "effort": "string",
        "impact": "string",
        "confidence": "number (0-100)"
      }
    ],
    "totalTokensUsed": "number"
  }
}
```

**Example**:
```json
{
  "repositoryFullName": "facebook/react",
  "overallScore": 87.5,
  "rating": "GOOD",
  "metrics": {
    "Documentation": {
      "name": "Documentation",
      "score": 100.0,
      "weight": 20.0,
      "weightedScore": 20.0,
      "description": "Evaluates the presence of essential documentation files",
      "details": "Found: README.md, CONTRIBUTING.md, LICENSE, CODE_OF_CONDUCT.md, CHANGELOG.md. Missing: none"
    },
    "Issue Management": {
      "name": "Issue Management",
      "score": 95.0,
      "weight": 20.0,
      "weightedScore": 19.0,
      "description": "Evaluates issue closure rate and responsiveness",
      "details": "Closure rate: 95.0%, Open issues: 450"
    }
  },
  "recommendation": "Good repository maintainability. Keep up the good work!"
}
```

---

## Exit Codes

| Code | Meaning | Description |
|------|---------|-------------|
| `0` | Success | Analysis completed successfully |
| `1` | User Error | Invalid arguments, missing required options |
| `2` | Execution Error | Runtime error during analysis (API failure, network issue) |
| `64` | Usage Error | Invalid command-line usage (Picocli standard) |
| `70` | Internal Error | Internal software error (unexpected exception) |

**Examples**:
```bash
# Success
$ rmi analyze facebook/react
$ echo $?
0

# Invalid arguments
$ rmi analyze invalid-format
$ echo $?
1

# API failure (network error)
$ rmi analyze facebook/react
Error: Failed to connect to GitHub API
$ echo $?
2
```

---

## Environment Variables

### GitHub Configuration

#### `GITHUB_TOKEN`

**Description**: GitHub personal access token for authentication  
**Type**: String (token)  
**Required**: No (but highly recommended)  
**Default**: None  
**Security**: Keep secret, never commit to version control

**Example**:
```bash
export GITHUB_TOKEN="ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
```

**Priority**: Environment variable < Command-line option  
If both are set, command-line option takes precedence.

### LLM Configuration

#### `OPENROUTER_API_KEY`

**Description**: API key for OpenRouter LLM service  
**Type**: String (API key)  
**Required**: Only if `--llm` flag is used  
**Default**: None  
**Security**: Keep secret, OpenRouter disables exposed keys

**Example**:
```bash
export OPENROUTER_API_KEY="sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
```

#### `OPENROUTER_MODEL`

**Description**: Default LLM model to use  
**Type**: String (model identifier)  
**Required**: No  
**Default**: `openai/gpt-oss-20b:free`

**Example**:
```bash
export OPENROUTER_MODEL="openai/gpt-4"
```

#### `OPENAI_API_BASE`

**Description**: OpenRouter API base URL  
**Type**: String (URL)  
**Required**: No  
**Default**: `https://openrouter.ai/api/v1`

**Example**:
```bash
export OPENAI_API_BASE="https://openrouter.ai/api/v1"
```

### Loading Environment Variables

RMI supports `.env` file for configuration:

**`.env` file**:
```bash
# GitHub API token
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# OpenRouter API configuration
OPENROUTER_API_KEY=sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
OPENROUTER_MODEL=openai/gpt-4
OPENAI_API_BASE=https://openrouter.ai/api/v1
```

**Location**: Project root directory or current working directory

---

## External API Dependencies

### GitHub REST API v3

**Base URL**: `https://api.github.com`  
**Documentation**: https://docs.github.com/en/rest  
**Version**: v3

#### Endpoints Used:

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/repos/:owner/:repo` | GET | Repository information |
| `/repos/:owner/:repo/commits` | GET | Commit history |
| `/repos/:owner/:repo/contents/:path` | GET | File existence check |
| `/repos/:owner/:repo/issues` | GET | Issue statistics |
| `/repos/:owner/:repo/branches` | GET | Branch count |
| `/repos/:owner/:repo/contributors` | GET | Contributor count |

#### Rate Limits:

| Authentication | Requests/Hour | Notes |
|----------------|---------------|-------|
| Unauthenticated | 60 | IP-based |
| Authenticated | 5,000 | Token-based |
| GitHub Actions | 15,000 | Special limit |

#### Response Headers:

- `X-RateLimit-Limit`: Maximum requests per hour
- `X-RateLimit-Remaining`: Remaining requests
- `X-RateLimit-Reset`: Unix timestamp when limit resets

### OpenRouter API (Optional)

**Base URL**: `https://openrouter.ai/api/v1`  
**Documentation**: https://openrouter.ai/docs  
**Format**: OpenAI-compatible

#### Endpoints Used:

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/chat/completions` | POST | LLM analysis |

#### Request Format:

```json
{
  "model": "string",
  "messages": [
    {
      "role": "user",
      "content": "string"
    }
  ],
  "temperature": 0.3,
  "max_tokens": 2000
}
```

#### Response Format:

```json
{
  "choices": [
    {
      "message": {
        "content": "string"
      }
    }
  ],
  "usage": {
    "total_tokens": 123
  }
}
```

---

## Error Handling

### Error Message Format

**Structure**:
```
Error: <error-type>: <error-message>
<additional-context>

Suggestion: <how-to-fix>
```

### Common Errors

#### 1. Repository Not Found

**Message**:
```
Error: Repository not found: owner/repo
GitHub API returned 404 Not Found

Suggestion: Verify the repository name and ensure it exists
```

**Exit Code**: 2  
**Cause**: Invalid repository name or private repo without access

#### 2. Authentication Error

**Message**:
```
Error: Authentication failed
GitHub API returned 401 Unauthorized

Suggestion: Check your GitHub token:
  1. Verify token is valid
  2. Ensure required scopes: public_repo, repo:status
  3. Set GITHUB_TOKEN environment variable or use --token option
```

**Exit Code**: 2  
**Cause**: Invalid or expired GitHub token

#### 3. Rate Limit Exceeded

**Message**:
```
Error: GitHub API rate limit exceeded
Limit: 60/hour (unauthenticated)
Reset at: 2025-01-17 15:30:00 UTC

Suggestion: Use GitHub token for higher rate limits (5000/hour):
  export GITHUB_TOKEN="your-token"
```

**Exit Code**: 2  
**Cause**: Too many API requests

#### 4. Network Error

**Message**:
```
Error: Network connection failed
Unable to reach api.github.com

Suggestion: Check your internet connection and firewall settings
```

**Exit Code**: 2  
**Cause**: Network connectivity issue

#### 5. Invalid Arguments

**Message**:
```
Error: Invalid repository format
Expected: OWNER/REPO (e.g., facebook/react)
Received: invalid-format

Suggestion: Use format: owner/repository
```

**Exit Code**: 1  
**Cause**: Malformed command-line arguments

#### 6. LLM API Error

**Message**:
```
Warning: LLM analysis failed
OpenRouter API error: Invalid API key

Note: Continuing with deterministic analysis only
```

**Exit Code**: 0 (graceful fallback)  
**Cause**: LLM service unavailable or invalid API key

---

## Examples

### Basic Usage

```bash
# Analyze public repository
$ rmi analyze facebook/react

# With GitHub token (recommended)
$ rmi analyze facebook/react --token ghp_xxxxxxxxxxxx

# JSON output
$ rmi analyze facebook/react --format json

# Save output to file
$ rmi analyze facebook/react > report.txt
$ rmi analyze facebook/react --format json > report.json
```

### With LLM Analysis

```bash
# Enable LLM analysis (requires OPENROUTER_API_KEY)
$ export OPENROUTER_API_KEY="sk-or-v1-xxx"
$ rmi analyze facebook/react --llm

# With custom model
$ rmi analyze facebook/react --llm --model openai/gpt-4

# Environment variables and LLM
$ OPENROUTER_API_KEY="sk-or-v1-xxx" \
  OPENROUTER_MODEL="openai/gpt-4" \
  rmi analyze facebook/react --llm
```

### Pipeline Integration

```bash
# CI/CD integration
$ rmi analyze $GITHUB_REPOSITORY --token $GITHUB_TOKEN --format json | \
  jq '.overallScore' | \
  awk '{ if ($1 < 70) exit 1 }'

# Batch analysis
$ for repo in facebook/react vercel/next.js; do
    echo "Analyzing $repo..."
    rmi analyze $repo --format json > "$(echo $repo | tr '/' '-').json"
  done

# Quality gate
$ SCORE=$(rmi analyze myorg/myrepo --format json | jq -r '.overallScore')
$ if (( $(echo "$SCORE < 75" | bc -l) )); then
    echo "Quality gate failed: score $SCORE < 75"
    exit 1
  fi
```

### Error Handling Examples

```bash
# Handle errors gracefully
$ rmi analyze invalid/repo || echo "Analysis failed with code $?"

# Retry on failure
$ for i in {1..3}; do
    rmi analyze facebook/react && break || sleep 5
  done

# Log output
$ rmi analyze facebook/react 2>&1 | tee analysis.log
```

---

## API Versioning

### Current Version: 1.0.0

**Versioning Scheme**: Semantic Versioning (SemVer)  
**Format**: MAJOR.MINOR.PATCH

**Version Compatibility**:
- **MAJOR**: Incompatible API changes
- **MINOR**: Backward-compatible functionality additions
- **PATCH**: Backward-compatible bug fixes

**Checking Version**:
```bash
$ rmi --version
Repository Maintainability Index CLI 1.0.0
```

---

## Future API Considerations

### Potential REST API (Future)

If RMI evolves to include a web service, the proposed REST API would be:

```
POST /api/v1/analyze
Content-Type: application/json

{
  "repository": "owner/repo",
  "options": {
    "includeLLM": true,
    "model": "openai/gpt-4"
  }
}

Response:
{
  "status": "success",
  "data": { ... },
  "metadata": {
    "analyzedAt": "2025-01-17T12:00:00Z",
    "version": "1.0.0"
  }
}
```

**Note**: This is **not currently implemented**. RMI is CLI-only.

---

**Document Version**: 1.0  
**Last Reviewed**: January 17, 2025  
**Next Review**: Q1 2025
