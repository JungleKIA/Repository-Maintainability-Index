# API Specification

**Application:** Repository Maintainability Index (RMI)  
**Version:** 1.0.0  
**API Type:** Command-Line Interface (CLI) + JSON Output  
**Last Updated:** 2024

---

## Table of Contents

1. [Overview](#overview)
2. [CLI Command Reference](#cli-command-reference)
3. [JSON Output Schema](#json-output-schema)
4. [Error Codes & Handling](#error-codes--handling)
5. [Rate Limits & Quotas](#rate-limits--quotas)
6. [Examples](#examples)
7. [Integration Guide](#integration-guide)

---

## Overview

RMI provides a command-line interface for analyzing GitHub repositories. The tool can output results in two formats:
- **Text Format** - Human-readable analysis reports with Unicode formatting
- **JSON Format** - Machine-readable structured data for integration

### Key Features
- ✅ **6 Maintainability Metrics** - Documentation, Commit Quality, Activity, Issues, Community, Branches
- ✅ **Optional AI Analysis** - LLM-powered insights (README, commits, community)
- ✅ **Multiple Output Formats** - Text or JSON
- ✅ **Flexible Authentication** - GitHub token via environment or CLI
- ✅ **Docker Support** - Containerized execution

---

## CLI Command Reference

### Root Command: `rmi`

#### Syntax
```bash
java -jar repo-maintainability-index-1.0.0.jar [OPTIONS] COMMAND [ARGS]
```

#### Global Options
| Option | Short | Description | Default |
|--------|-------|-------------|---------|
| `--help` | `-h` | Display help information | N/A |
| `--version` | `-V` | Display version information | N/A |

#### Example
```bash
java -jar rmi.jar --help
java -jar rmi.jar --version
```

---

### Command: `analyze`

Analyzes a GitHub repository and generates a maintainability report.

#### Syntax
```bash
java -jar rmi.jar analyze <owner/repo> [OPTIONS]
```

#### Required Arguments
| Argument | Format | Description | Example |
|----------|--------|-------------|---------|
| `repository` | `owner/repo` | GitHub repository identifier | `facebook/react` |

#### Options
| Option | Short | Type | Description | Default | Required |
|--------|-------|------|-------------|---------|----------|
| `--token` | `-t` | String | GitHub personal access token | `$GITHUB_TOKEN` | No |
| `--llm` | N/A | Boolean | Enable LLM-powered AI analysis | `false` | No |
| `--model` | `-m` | String | LLM model to use (requires --llm) | `openai/gpt-3.5-turbo` | No |
| `--format` | `-f` | Enum | Output format (`text` or `json`) | `text` | No |
| `--help` | `-h` | N/A | Display command help | N/A | No |

#### Environment Variables
| Variable | Description | Required | Priority |
|----------|-------------|----------|----------|
| `GITHUB_TOKEN` | GitHub personal access token | Recommended | CLI `--token` overrides |
| `OPENROUTER_API_KEY` | OpenRouter API key for LLM | Required if `--llm` | N/A |
| `OPENROUTER_MODEL` | Default LLM model | No | CLI `--model` overrides |

#### Exit Codes
| Code | Meaning | Description |
|------|---------|-------------|
| `0` | Success | Analysis completed successfully |
| `1` | Parse Error | Invalid command-line arguments |
| `2` | Execution Error | Analysis failed (GitHub API error, network issue, etc.) |

#### Examples

**Basic Analysis**
```bash
java -jar rmi.jar analyze facebook/react
```

**With GitHub Token**
```bash
java -jar rmi.jar analyze facebook/react --token ghp_xxxxxxxxxxxx
# Or via environment
export GITHUB_TOKEN=ghp_xxxxxxxxxxxx
java -jar rmi.jar analyze facebook/react
```

**With AI Analysis**
```bash
export GITHUB_TOKEN=ghp_xxxxxxxxxxxx
export OPENROUTER_API_KEY=sk-or-xxxxxxxxxxxx
java -jar rmi.jar analyze facebook/react --llm
```

**JSON Output**
```bash
java -jar rmi.jar analyze facebook/react --format json
```

**Custom LLM Model**
```bash
java -jar rmi.jar analyze facebook/react --llm --model openai/gpt-4
```

**Full Example**
```bash
java -jar rmi.jar analyze facebook/react \
  --token ghp_xxxxxxxxxxxx \
  --llm \
  --model openai/gpt-4 \
  --format json
```

---

## JSON Output Schema

### Root Object: `MaintainabilityReport`

```json
{
  "repository": {
    "owner": "string",
    "name": "string",
    "fullName": "string",
    "description": "string | null",
    "url": "string",
    "language": "string | null",
    "stars": "integer",
    "forks": "integer",
    "openIssues": "integer",
    "createdAt": "string (ISO 8601)",
    "updatedAt": "string (ISO 8601)"
  },
  "analysis": {
    "overallScore": "number (0-100)",
    "rating": "string (EXCELLENT | GOOD | FAIR | POOR)",
    "analyzedAt": "string (ISO 8601)",
    "analysisVersion": "string"
  },
  "metrics": [
    {
      "name": "string",
      "score": "number (0-100)",
      "weight": "number (0-1)",
      "weightedScore": "number",
      "description": "string",
      "details": "string"
    }
  ],
  "llmAnalysis": {
    "readme": {
      "clarityScore": "number (0-100)",
      "completenessScore": "number (0-100)",
      "newcomerFriendlinessScore": "number (0-100)",
      "strengths": ["string"],
      "weaknesses": ["string"],
      "suggestions": ["string"]
    },
    "commitQuality": {
      "consistencyScore": "number (0-100)",
      "descriptionQualityScore": "number (0-100)",
      "patterns": ["string"],
      "suggestions": ["string"]
    },
    "communityHealth": {
      "responsivenessScore": "number (0-100)",
      "helpfulnessScore": "number (0-100)",
      "toneScore": "number (0-100)",
      "strengths": ["string"],
      "concerns": ["string"],
      "suggestions": ["string"]
    },
    "recommendations": [
      {
        "title": "string",
        "description": "string",
        "impact": "string (HIGH | MEDIUM | LOW)",
        "confidence": "number (0-100)",
        "category": "string"
      }
    ]
  },
  "recommendation": "string"
}
```

### Field Descriptions

#### `repository` Object
| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `owner` | String | Repository owner username | `"facebook"` |
| `name` | String | Repository name | `"react"` |
| `fullName` | String | Full repository identifier | `"facebook/react"` |
| `description` | String, null | Repository description | `"A declarative..."` |
| `url` | String | GitHub repository URL | `"https://github.com/..."` |
| `language` | String, null | Primary programming language | `"JavaScript"` |
| `stars` | Integer | Number of stars | `200000` |
| `forks` | Integer | Number of forks | `40000` |
| `openIssues` | Integer | Number of open issues | `1500` |
| `createdAt` | String | Creation timestamp (ISO 8601) | `"2013-05-24T16:15:54Z"` |
| `updatedAt` | String | Last update timestamp (ISO 8601) | `"2024-01-15T10:30:00Z"` |

#### `analysis` Object
| Field | Type | Description | Range |
|-------|------|-------------|-------|
| `overallScore` | Number | Weighted average of all metrics | `0-100` |
| `rating` | Enum | Quality rating | `EXCELLENT`, `GOOD`, `FAIR`, `POOR` |
| `analyzedAt` | String | Analysis timestamp (ISO 8601) | ISO 8601 format |
| `analysisVersion` | String | RMI version used | `"1.0.0"` |

##### Rating Thresholds
- **EXCELLENT**: `overallScore >= 90`
- **GOOD**: `overallScore >= 70`
- **FAIR**: `overallScore >= 50`
- **POOR**: `overallScore < 50`

#### `metrics` Array
Each metric object contains:

| Field | Type | Description | Range |
|-------|------|-------------|-------|
| `name` | String | Metric name | See [Metrics Reference](#metrics-reference) |
| `score` | Number | Raw metric score | `0-100` |
| `weight` | Number | Metric weight in overall score | `0-1` |
| `weightedScore` | Number | `score * weight * 100` | `0-100` |
| `description` | String | Human-readable description | Text |
| `details` | String | Detailed findings and data | Text |

##### Metrics Reference
| Metric Name | Weight | Description |
|-------------|--------|-------------|
| `Documentation` | 20% | Presence of essential documentation files |
| `Issue Management` | 20% | Issue closure rate and open issue count |
| `Commit Quality` | 15% | Adherence to commit message conventions |
| `Activity` | 15% | Repository freshness based on recent commits |
| `Community` | 15% | Stars, forks, and contributor count |
| `Branch Management` | 15% | Number of branches (fewer is better) |

#### `llmAnalysis` Object (Optional)
Only present when `--llm` flag is used and LLM analysis succeeds.

##### `readme` Object
| Field | Type | Description | Range |
|-------|------|-------------|-------|
| `clarityScore` | Number | README clarity rating | `0-100` |
| `completenessScore` | Number | Documentation completeness | `0-100` |
| `newcomerFriendlinessScore` | Number | Ease of onboarding | `0-100` |
| `strengths` | String[] | Identified strengths | Array of strings |
| `weaknesses` | String[] | Identified weaknesses | Array of strings |
| `suggestions` | String[] | Improvement suggestions | Array of strings |

##### `commitQuality` Object
| Field | Type | Description | Range |
|-------|------|-------------|-------|
| `consistencyScore` | Number | Commit message consistency | `0-100` |
| `descriptionQualityScore` | Number | Quality of descriptions | `0-100` |
| `patterns` | String[] | Identified patterns | Array of strings |
| `suggestions` | String[] | Improvement suggestions | Array of strings |

##### `communityHealth` Object
| Field | Type | Description | Range |
|-------|------|-------------|-------|
| `responsivenessScore` | Number | Maintainer responsiveness | `0-100` |
| `helpfulnessScore` | Number | Community helpfulness | `0-100` |
| `toneScore` | Number | Communication tone quality | `0-100` |
| `strengths` | String[] | Community strengths | Array of strings |
| `concerns` | String[] | Identified concerns | Array of strings |
| `suggestions` | String[] | Improvement suggestions | Array of strings |

##### `recommendations` Array
| Field | Type | Description | Values |
|-------|------|-------------|--------|
| `title` | String | Recommendation title | Text |
| `description` | String | Detailed description | Text |
| `impact` | Enum | Expected impact level | `HIGH`, `MEDIUM`, `LOW` |
| `confidence` | Number | AI confidence in recommendation | `0-100` |
| `category` | String | Recommendation category | `Documentation`, `Process`, `Community`, etc. |

#### `recommendation` String
High-level summary and overall recommendation based on the analysis.

---

### JSON Schema (JSON Schema Draft 7)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "MaintainabilityReport",
  "type": "object",
  "required": ["repository", "analysis", "metrics", "recommendation"],
  "properties": {
    "repository": {
      "type": "object",
      "required": ["owner", "name", "fullName", "url", "stars", "forks", "openIssues", "createdAt", "updatedAt"],
      "properties": {
        "owner": { "type": "string" },
        "name": { "type": "string" },
        "fullName": { "type": "string" },
        "description": { "type": ["string", "null"] },
        "url": { "type": "string", "format": "uri" },
        "language": { "type": ["string", "null"] },
        "stars": { "type": "integer", "minimum": 0 },
        "forks": { "type": "integer", "minimum": 0 },
        "openIssues": { "type": "integer", "minimum": 0 },
        "createdAt": { "type": "string", "format": "date-time" },
        "updatedAt": { "type": "string", "format": "date-time" }
      }
    },
    "analysis": {
      "type": "object",
      "required": ["overallScore", "rating", "analyzedAt", "analysisVersion"],
      "properties": {
        "overallScore": { "type": "number", "minimum": 0, "maximum": 100 },
        "rating": { "type": "string", "enum": ["EXCELLENT", "GOOD", "FAIR", "POOR"] },
        "analyzedAt": { "type": "string", "format": "date-time" },
        "analysisVersion": { "type": "string" }
      }
    },
    "metrics": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["name", "score", "weight", "weightedScore", "description", "details"],
        "properties": {
          "name": { "type": "string" },
          "score": { "type": "number", "minimum": 0, "maximum": 100 },
          "weight": { "type": "number", "minimum": 0, "maximum": 1 },
          "weightedScore": { "type": "number", "minimum": 0, "maximum": 100 },
          "description": { "type": "string" },
          "details": { "type": "string" }
        }
      }
    },
    "llmAnalysis": {
      "type": "object",
      "properties": {
        "readme": {
          "type": "object",
          "required": ["clarityScore", "completenessScore", "newcomerFriendlinessScore"],
          "properties": {
            "clarityScore": { "type": "number", "minimum": 0, "maximum": 100 },
            "completenessScore": { "type": "number", "minimum": 0, "maximum": 100 },
            "newcomerFriendlinessScore": { "type": "number", "minimum": 0, "maximum": 100 },
            "strengths": { "type": "array", "items": { "type": "string" } },
            "weaknesses": { "type": "array", "items": { "type": "string" } },
            "suggestions": { "type": "array", "items": { "type": "string" } }
          }
        },
        "commitQuality": {
          "type": "object",
          "properties": {
            "consistencyScore": { "type": "number", "minimum": 0, "maximum": 100 },
            "descriptionQualityScore": { "type": "number", "minimum": 0, "maximum": 100 },
            "patterns": { "type": "array", "items": { "type": "string" } },
            "suggestions": { "type": "array", "items": { "type": "string" } }
          }
        },
        "communityHealth": {
          "type": "object",
          "properties": {
            "responsivenessScore": { "type": "number", "minimum": 0, "maximum": 100 },
            "helpfulnessScore": { "type": "number", "minimum": 0, "maximum": 100 },
            "toneScore": { "type": "number", "minimum": 0, "maximum": 100 },
            "strengths": { "type": "array", "items": { "type": "string" } },
            "concerns": { "type": "array", "items": { "type": "string" } },
            "suggestions": { "type": "array", "items": { "type": "string" } }
          }
        },
        "recommendations": {
          "type": "array",
          "items": {
            "type": "object",
            "required": ["title", "description", "impact", "confidence", "category"],
            "properties": {
              "title": { "type": "string" },
              "description": { "type": "string" },
              "impact": { "type": "string", "enum": ["HIGH", "MEDIUM", "LOW"] },
              "confidence": { "type": "number", "minimum": 0, "maximum": 100 },
              "category": { "type": "string" }
            }
          }
        }
      }
    },
    "recommendation": { "type": "string" }
  }
}
```

---

## Error Codes & Handling

### CLI Exit Codes
| Code | Category | Description | Action |
|------|----------|-------------|--------|
| `0` | Success | Analysis completed successfully | Continue normal flow |
| `1` | Parse Error | Invalid command-line arguments | Check command syntax |
| `2` | Execution Error | Analysis failed | Check error message for details |

### Common Error Scenarios

#### GitHub API Errors
| Error Message | Cause | Solution |
|---------------|-------|----------|
| `Repository not found` | Invalid repository name or private repo | Verify repository exists and is public |
| `API rate limit exceeded` | Too many requests | Wait or use authenticated requests |
| `Bad credentials` | Invalid GitHub token | Verify `GITHUB_TOKEN` is valid |
| `Not Found (404)` | Repository deleted or renamed | Update repository identifier |
| `Forbidden (403)` | Access denied | Check repository permissions |

#### Network Errors
| Error Message | Cause | Solution |
|---------------|-------|----------|
| `SocketTimeoutException` | Network timeout | Check internet connection, retry |
| `UnknownHostException` | DNS resolution failure | Check DNS, firewall settings |
| `ConnectException` | Connection refused | Check network connectivity |

#### LLM API Errors (Non-Fatal)
| Error Message | Cause | Solution |
|---------------|-------|----------|
| `LLM analysis failed` | OpenRouter API error | Check `OPENROUTER_API_KEY`, analysis continues |
| `Invalid API key` | Incorrect LLM API key | Update `OPENROUTER_API_KEY` |
| `Rate limit exceeded` | LLM quota exhausted | Wait or upgrade plan |

#### Configuration Errors
| Error Message | Cause | Solution |
|---------------|-------|----------|
| `Missing required argument` | No repository specified | Provide `owner/repo` argument |
| `Invalid output format` | Unsupported format | Use `text` or `json` |
| `Model not found` | Invalid LLM model name | Check available models |

### Error Response Format

#### Text Format
```
Error: <error_message>
Details: <detailed_explanation>
Suggestion: <how_to_fix>
```

#### JSON Format
```json
{
  "error": true,
  "message": "Error description",
  "code": "ERROR_CODE",
  "details": "Detailed explanation",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Rate Limits & Quotas

### GitHub API Rate Limits

#### Unauthenticated Requests
- **Limit:** 60 requests/hour per IP address
- **Recommended:** Always use authentication

#### Authenticated Requests
- **Limit:** 5,000 requests/hour per token
- **Typical Analysis:** Uses 5-10 API calls
- **Capacity:** ~500-1000 analyses/hour

#### Rate Limit Headers (GitHub Response)
```
X-RateLimit-Limit: 5000
X-RateLimit-Remaining: 4995
X-RateLimit-Reset: 1644945600
```

#### Check Rate Limit
```bash
curl -H "Authorization: Bearer $GITHUB_TOKEN" https://api.github.com/rate_limit
```

### OpenRouter API Limits
- **Free Tier:** Varies by model (typically 10-20 requests/min)
- **Paid Tier:** Higher limits based on plan
- **Typical Analysis:** 3 LLM calls (README, commits, community)
- **Rate Limits:** Managed by OpenRouter per API key

### Handling Rate Limits

#### GitHub Rate Limit Exceeded
```bash
# Error output
Error: GitHub API rate limit exceeded
Reset time: 2024-01-15T11:00:00Z (in 25 minutes)

# Solutions:
1. Wait until reset time
2. Use authenticated requests (GITHUB_TOKEN)
3. Use multiple tokens with rotation (advanced)
```

#### LLM Rate Limit Exceeded
```bash
# Warning output (analysis continues)
Warning: LLM analysis failed due to rate limit
Continuing with deterministic metrics only...

# Solutions:
1. Wait and retry
2. Upgrade OpenRouter plan
3. Use different model with higher limits
```

---

## Examples

### Example 1: Basic Analysis (Text Output)

**Command:**
```bash
java -jar rmi.jar analyze prettier/prettier
```

**Output:**
```
═══════════════════════════════════════════════════════════════
  Repository Maintainability Index Report
═══════════════════════════════════════════════════════════════

Repository: prettier/prettier
Overall Score: 87.50/100
Rating: GOOD

───────────────────────────────────────────────────────────────
  Detailed Metrics
───────────────────────────────────────────────────────────────

▪ Documentation: 100.00/100 (weight: 20%)
  Evaluates the presence of essential documentation files
  Details: Found: README.md, CONTRIBUTING.md, LICENSE. Missing: none

▪ Commit Quality: 85.00/100 (weight: 15%)
  Evaluates commit message quality and conventions
  Details: Analyzed 50 commits: 42 (85.0%) follow conventions

▪ Activity: 95.00/100 (weight: 15%)
  Evaluates repository freshness based on recent commits
  Details: Last commit: 2 days ago

▪ Issue Management: 80.00/100 (weight: 20%)
  Evaluates issue closure rate and open issue count
  Details: Issue closure rate: 90.5%, Open issues: 350

▪ Community: 90.00/100 (weight: 15%)
  Evaluates stars, forks, and contributor engagement
  Details: Stars: 45000, Forks: 4000, Contributors: 500

▪ Branch Management: 85.00/100 (weight: 15%)
  Evaluates branch management practices
  Details: Active branches: 15

───────────────────────────────────────────────────────────────
  Recommendation
───────────────────────────────────────────────────────────────

Good repository maintainability. Keep up the good work!

═══════════════════════════════════════════════════════════════
```

### Example 2: JSON Output

**Command:**
```bash
java -jar rmi.jar analyze prettier/prettier --format json
```

**Output:**
```json
{
  "repository": {
    "owner": "prettier",
    "name": "prettier",
    "fullName": "prettier/prettier",
    "description": "Prettier is an opinionated code formatter",
    "url": "https://github.com/prettier/prettier",
    "language": "JavaScript",
    "stars": 45000,
    "forks": 4000,
    "openIssues": 350,
    "createdAt": "2017-01-10T18:12:00Z",
    "updatedAt": "2024-01-15T10:00:00Z"
  },
  "analysis": {
    "overallScore": 87.5,
    "rating": "GOOD",
    "analyzedAt": "2024-01-15T12:30:00Z",
    "analysisVersion": "1.0.0"
  },
  "metrics": [
    {
      "name": "Documentation",
      "score": 100.0,
      "weight": 0.20,
      "weightedScore": 20.0,
      "description": "Evaluates the presence of essential documentation files",
      "details": "Found: README.md, CONTRIBUTING.md, LICENSE. Missing: none"
    },
    {
      "name": "Commit Quality",
      "score": 85.0,
      "weight": 0.15,
      "weightedScore": 12.75,
      "description": "Evaluates commit message quality and conventions",
      "details": "Analyzed 50 commits: 42 (85.0%) follow conventions"
    }
  ],
  "recommendation": "Good repository maintainability. Keep up the good work!"
}
```

### Example 3: With AI Analysis

**Command:**
```bash
export GITHUB_TOKEN=ghp_xxxxxxxxxxxx
export OPENROUTER_API_KEY=sk-or-xxxxxxxxxxxx
java -jar rmi.jar analyze prettier/prettier --llm --format json
```

**Output:** (Includes `llmAnalysis` object)
```json
{
  "repository": { "...": "..." },
  "analysis": { "...": "..." },
  "metrics": [ "..." ],
  "llmAnalysis": {
    "readme": {
      "clarityScore": 92,
      "completenessScore": 88,
      "newcomerFriendlinessScore": 85,
      "strengths": [
        "Clear installation instructions",
        "Comprehensive examples",
        "Well-organized structure"
      ],
      "weaknesses": [
        "Missing troubleshooting section",
        "Limited architecture overview"
      ],
      "suggestions": [
        "Add troubleshooting guide",
        "Include architecture diagram"
      ]
    },
    "commitQuality": {
      "consistencyScore": 90,
      "descriptionQualityScore": 85,
      "patterns": [
        "Conventional Commits format widely used",
        "Clear, descriptive commit messages"
      ],
      "suggestions": [
        "Enforce commit message format with pre-commit hook",
        "Add commit message template"
      ]
    },
    "communityHealth": {
      "responsivenessScore": 88,
      "helpfulnessScore": 92,
      "toneScore": 95,
      "strengths": [
        "Quick response times",
        "Helpful and constructive feedback",
        "Welcoming tone"
      ],
      "concerns": [],
      "suggestions": [
        "Consider adding more maintainers for faster response"
      ]
    },
    "recommendations": [
      {
        "title": "Add Architecture Documentation",
        "description": "Include high-level architecture diagrams to help new contributors understand the codebase structure",
        "impact": "HIGH",
        "confidence": 85,
        "category": "Documentation"
      },
      {
        "title": "Implement Commit Message Linting",
        "description": "Add commitlint or similar tool to enforce consistent commit message format",
        "impact": "MEDIUM",
        "confidence": 90,
        "category": "Process"
      }
    ]
  },
  "recommendation": "Excellent repository with strong community and documentation. Focus on architecture documentation and commit automation."
}
```

---

## Integration Guide

### Shell Script Integration

```bash
#!/bin/bash
set -e

REPO=$1
OUTPUT_FILE="analysis-$(date +%Y%m%d-%H%M%S).json"

# Run analysis
java -jar rmi.jar analyze "$REPO" --format json > "$OUTPUT_FILE"

# Check exit code
if [ $? -eq 0 ]; then
  echo "Analysis completed: $OUTPUT_FILE"
  
  # Extract overall score
  SCORE=$(jq '.analysis.overallScore' "$OUTPUT_FILE")
  echo "Score: $SCORE/100"
  
  # Fail if score below threshold
  if (( $(echo "$SCORE < 70" | bc -l) )); then
    echo "ERROR: Score below threshold (70)"
    exit 1
  fi
else
  echo "ERROR: Analysis failed"
  exit 2
fi
```

### Python Integration

```python
import subprocess
import json
import sys

def analyze_repository(owner_repo, github_token=None):
    """Analyze a GitHub repository using RMI."""
    
    cmd = [
        'java', '-jar', 'rmi.jar',
        'analyze', owner_repo,
        '--format', 'json'
    ]
    
    if github_token:
        cmd.extend(['--token', github_token])
    
    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            check=True
        )
        
        # Parse JSON output
        analysis = json.loads(result.stdout)
        return analysis
        
    except subprocess.CalledProcessError as e:
        print(f"Analysis failed: {e.stderr}", file=sys.stderr)
        return None

# Usage
analysis = analyze_repository('prettier/prettier')
if analysis:
    print(f"Score: {analysis['analysis']['overallScore']}/100")
    print(f"Rating: {analysis['analysis']['rating']}")
```

### JavaScript/Node.js Integration

```javascript
const { execSync } = require('child_process');

function analyzeRepository(ownerRepo, options = {}) {
  const cmd = [
    'java', '-jar', 'rmi.jar',
    'analyze', ownerRepo,
    '--format', 'json'
  ];
  
  if (options.githubToken) {
    cmd.push('--token', options.githubToken);
  }
  
  if (options.llm) {
    cmd.push('--llm');
  }
  
  try {
    const output = execSync(cmd.join(' '), { encoding: 'utf-8' });
    return JSON.parse(output);
  } catch (error) {
    console.error('Analysis failed:', error.message);
    return null;
  }
}

// Usage
const analysis = analyzeRepository('prettier/prettier', {
  githubToken: process.env.GITHUB_TOKEN,
  llm: true
});

if (analysis) {
  console.log(`Score: ${analysis.analysis.overallScore}/100`);
  console.log(`Rating: ${analysis.analysis.rating}`);
}
```

### CI/CD Integration (GitHub Actions)

```yaml
name: Repository Quality Check

on:
  schedule:
    - cron: '0 0 * * 0'  # Weekly on Sunday
  workflow_dispatch:

jobs:
  quality-check:
    runs-on: ubuntu-latest
    steps:
      - name: Download RMI
        run: |
          wget https://github.com/owner/rmi/releases/latest/download/rmi.jar
      
      - name: Analyze Repository
        id: analyze
        run: |
          java -jar rmi.jar analyze ${{ github.repository }} \
            --token ${{ secrets.GITHUB_TOKEN }} \
            --format json > analysis.json
          
          SCORE=$(jq '.analysis.overallScore' analysis.json)
          echo "score=$SCORE" >> $GITHUB_OUTPUT
      
      - name: Check Quality Gate
        run: |
          if (( $(echo "${{ steps.analyze.outputs.score }} < 70" | bc -l) )); then
            echo "Quality gate failed: Score is ${{ steps.analyze.outputs.score }}/100 (threshold: 70)"
            exit 1
          fi
      
      - name: Upload Analysis
        uses: actions/upload-artifact@v3
        with:
          name: quality-analysis
          path: analysis.json
```

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Maintained By:** Engineering Team
