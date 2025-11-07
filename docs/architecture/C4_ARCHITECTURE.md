# C4 Architecture Model

This document describes the Repository Maintainability Index architecture using the C4 model (Context and Container levels).

## Level 1: System Context Diagram

Shows how RMI fits into the overall landscape and who uses it.

```
                    ┌──────────────────┐
                    │                  │
                    │   Developer /    │
                    │  DevOps Engineer │
                    │                  │
                    └────────┬─────────┘
                             │
                             │ Analyzes repositories
                             │ via CLI
                             │
                             ▼
           ┌─────────────────────────────────┐
           │                                 │
           │  Repository Maintainability     │
           │         Index (RMI)             │
           │                                 │
           │  Analyzes GitHub repositories   │
           │  for quality and maintainability│
           │                                 │
           └────────┬──────────────┬─────────┘
                    │              │
                    │              │
        ┌───────────┘              └──────────┐
        │                                     │
        │ Fetches repo data                  │ Optional: AI analysis
        │ (REST API)                          │ (REST API)
        │                                     │
        ▼                                     ▼
┌───────────────┐                    ┌──────────────┐
│               │                    │              │
│  GitHub API   │                    │  OpenRouter  │
│               │                    │   API (LLM)  │
│  Provides     │                    │              │
│  repository   │                    │  Provides    │
│  metrics      │                    │  AI insights │
│               │                    │              │
└───────────────┘                    └──────────────┘
```

### External Dependencies

| System | Purpose | Protocol | Criticality |
|--------|---------|----------|-------------|
| GitHub API | Fetch repository metadata, commits, issues | REST (HTTPS) | Critical |
| OpenRouter API | Optional LLM-based analysis | REST (HTTPS) | Optional |

## Level 2: Container Diagram

Shows the high-level technical components of RMI.

```
┌─────────────────────────────────────────────────────────────────┐
│                    RMI Application (Java 17)                    │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    CLI Layer                             │  │
│  │  ┌────────────┐  ┌──────────────┐  ┌─────────────┐      │  │
│  │  │  Main.java │─▶│AnalyzeCommand│─▶│OutputFormatter│    │  │
│  │  └────────────┘  └──────┬───────┘  └─────────────┘      │  │
│  │                          │                               │  │
│  │                    (Picocli Framework)                   │  │
│  └──────────────────────────┼───────────────────────────────┘  │
│                             │                                  │
│                             ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  Service Layer                           │  │
│  │  ┌─────────────────────────────────────────────────┐     │  │
│  │  │   AnalysisService                               │     │  │
│  │  │   - Orchestrates analysis workflow              │     │  │
│  │  │   - Aggregates metrics                          │     │  │
│  │  │   - Computes weighted scores                    │     │  │
│  │  └─────────┬──────────────────────┬─────────────┬──┘     │  │
│  └────────────┼──────────────────────┼─────────────┼────────┘  │
│               │                      │             │           │
│               ▼                      ▼             ▼           │
│  ┌────────────────────┐  ┌──────────────┐  ┌────────────────┐ │
│  │   Metrics Layer    │  │GitHub Client │  │   LLM Client   │ │
│  │                    │  │              │  │                │ │
│  │ ┌────────────────┐ │  │┌───────────┐ │  │┌──────────────┐│ │
│  │ │Documentation   │ │  ││GitHubAPI  │ │  ││OpenRouterAPI││ │
│  │ │MetricCalculator│ │  ││Client     │ │  ││Client        ││ │
│  │ └────────────────┘ │  │└─────┬─────┘ │  │└──────┬───────┘│ │
│  │                    │  │      │       │  │       │        │ │
│  │ ┌────────────────┐ │  │      │OkHttp │  │       │OkHttp  │ │
│  │ │CommitQuality   │ │  │      │       │  │       │        │ │
│  │ │MetricCalculator│ │  │      │       │  │       │        │ │
│  │ └────────────────┘ │  └──────┼───────┘  └───────┼────────┘ │
│  │                    │         │                  │          │
│  │ ┌────────────────┐ │         │                  │          │
│  │ │Activity        │ │         │                  │          │
│  │ │MetricCalculator│ │         │                  │          │
│  │ └────────────────┘ │         │                  │          │
│  │                    │         │                  │          │
│  │ ┌────────────────┐ │         │                  │          │
│  │ │IssueManagement │ │         │                  │          │
│  │ │MetricCalculator│ │         │                  │          │
│  │ └────────────────┘ │         │                  │          │
│  │                    │         │                  │          │
│  │ ┌────────────────┐ │         │                  │          │
│  │ │Community       │ │         │                  │          │
│  │ │MetricCalculator│ │         │                  │          │
│  │ └────────────────┘ │         │                  │          │
│  │                    │         │                  │          │
│  │ ┌────────────────┐ │         │                  │          │
│  │ │BranchManagement│ │         │                  │          │
│  │ │MetricCalculator│ │         │                  │          │
│  │ └────────────────┘ │         │                  │          │
│  └────────────────────┘         │                  │          │
│                                  │                  │          │
│  ┌──────────────────────────────┼──────────────────┼────────┐ │
│  │         Model Layer          │                  │        │ │
│  │  (Immutable Domain Objects)  │                  │        │ │
│  │                              │                  │        │ │
│  │  • AnalysisResult            │                  │        │ │
│  │  • MetricResult              │                  │        │ │
│  │  • RepositoryInfo            │                  │        │ │
│  │  • LLMAnalysisResult         │                  │        │ │
│  └──────────────────────────────┼──────────────────┼────────┘ │
└─────────────────────────────────┼──────────────────┼──────────┘
                                  │                  │
                                  ▼                  ▼
                          ┌──────────────┐  ┌──────────────┐
                          │  GitHub API  │  │ OpenRouter   │
                          │  (External)  │  │ API (Ext)    │
                          └──────────────┘  └──────────────┘
```

## Container Details

### CLI Layer
- **Technology**: Picocli 4.7.5
- **Responsibility**: Command-line interface, argument parsing, output formatting
- **Key Components**:
  - `Main.java`: Application entry point
  - `AnalyzeCommand`: Command implementation
  - `OutputFormatter`: Text and JSON output formatting

### Service Layer
- **Technology**: Plain Java with SLF4J logging
- **Responsibility**: Business logic orchestration, metric aggregation
- **Key Components**:
  - `AnalysisService`: Coordinates the analysis workflow

### Metrics Layer
- **Technology**: Plain Java
- **Responsibility**: Individual metric calculations
- **Pattern**: Strategy pattern via `MetricCalculator` interface
- **Calculators**:
  - Documentation (20% weight)
  - Issue Management (20% weight)
  - Commit Quality (15% weight)
  - Activity (15% weight)
  - Community (15% weight)
  - Branch Management (15% weight)

### GitHub Client
- **Technology**: OkHttp 4.12.0 + Gson 2.10.1
- **Responsibility**: GitHub REST API communication
- **Features**: Rate limiting handling, authentication

### LLM Client
- **Technology**: OkHttp 4.12.0 + Gson 2.10.1
- **Responsibility**: OpenRouter API communication for AI analysis
- **Features**: Graceful fallback on errors, multiple model support

### Model Layer
- **Technology**: Plain Java with Builder pattern
- **Responsibility**: Domain object definitions
- **Pattern**: Immutable objects with defensive copying
- **Key Models**:
  - `AnalysisResult`: Overall analysis result
  - `MetricResult`: Individual metric result
  - `RepositoryInfo`: GitHub repository metadata
  - `LLMAnalysisResult`: AI analysis results

## Data Flow

### Standard Analysis Flow

```
User Command
     │
     ▼
AnalyzeCommand (CLI)
     │
     ▼
AnalysisService.analyze()
     │
     ├──▶ GitHubAPIClient.fetchRepositoryInfo() ──▶ GitHub API
     │        │
     │        ▼
     │    RepositoryInfo
     │
     ├──▶ DocumentationMetricCalculator.calculate()
     ├──▶ CommitQualityMetricCalculator.calculate()
     ├──▶ ActivityMetricCalculator.calculate()
     ├──▶ IssueManagementMetricCalculator.calculate()
     ├──▶ CommunityMetricCalculator.calculate()
     └──▶ BranchManagementMetricCalculator.calculate()
          │
          ▼
     Aggregate MetricResults
          │
          ▼
     Calculate Weighted Score
          │
          ▼
     AnalysisResult
          │
          ▼
OutputFormatter
          │
          ▼
     Console Output
```

### LLM-Enhanced Analysis Flow

```
Standard Analysis Flow (above)
     │
     ▼
AnalysisResult (with deterministic metrics)
     │
     ▼
LLMAnalysisService.analyze() (if --llm flag)
     │
     ├──▶ analyzeReadme() ──▶ OpenRouter API
     │         │
     │         ▼
     │    ReadmeAnalysis
     │
     ├──▶ analyzeCommitQuality() ──▶ OpenRouter API
     │         │
     │         ▼
     │    CommitQualityAnalysis
     │
     └──▶ analyzeCommunityHealth() ──▶ OpenRouter API
          │
          ▼
     CommunityHealthAnalysis
          │
          ▼
     LLMAnalysisResult
          │
          ▼
     Enhanced AnalysisResult
          │
          ▼
OutputFormatter (with AI insights)
          │
          ▼
     Console Output
```

## Error Handling Strategy

1. **GitHub API Failures**: Return error to user (critical path)
2. **LLM API Failures**: Graceful fallback to deterministic-only analysis (non-critical)
3. **Network Issues**: Retry with exponential backoff (GitHub), immediate fallback (LLM)
4. **Rate Limiting**: Respect GitHub rate limits, inform user

## Performance Characteristics

- **Cold Start**: ~1-2 seconds (JVM startup + dependency loading)
- **API Latency**: 
  - GitHub API: 200-500ms per endpoint
  - OpenRouter API: 2-10 seconds (LLM processing)
- **Memory Usage**: ~50-100 MB heap
- **Concurrency**: Sequential processing (CLI tool, no parallelism needed)

## Security Considerations

1. **API Tokens**: Environment variables only, never hardcoded
2. **TLS**: All external communication over HTTPS
3. **Dependencies**: Regular security scanning recommended
4. **Input Validation**: Repository names validated against GitHub format
5. **Secrets Management**: OpenRouter auto-disables exposed keys

## Scalability Notes

This is a **CLI tool** designed for:
- **Single-user execution**
- **Sequential processing**
- **Local resource constraints**

It is **NOT designed for**:
- High concurrency
- Horizontal scaling
- Multi-tenant scenarios
- Real-time streaming

For web service or high-throughput scenarios, a different architecture would be required.

## Technology Stack Summary

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Build | Maven | 3.6+ |
| CLI Framework | Picocli | 4.7.5 |
| HTTP Client | OkHttp | 4.12.0 |
| JSON | Gson | 2.10.1 |
| Logging | SLF4J + Logback | 2.0.9 / 1.4.14 |
| Testing | JUnit 5 | 5.10.1 |
| Mocking | Mockito | 5.7.0 |
| Assertions | AssertJ | 3.24.2 |

## References

- [C4 Model](https://c4model.com/)
- [Architecture Decision Records](adr/)
- [Main README](../../README.md)
