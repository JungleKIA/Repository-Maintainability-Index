# Repository Maintainability Index (RMI)

A production-level command-line tool for automated evaluation of GitHub repository quality and maintainability.

## Overview

The Repository Maintainability Index tool analyzes GitHub repositories and provides a comprehensive assessment of their quality based on multiple metrics including documentation, commit quality, activity, issue management, community engagement, and branch management.

## Features

- **Comprehensive Metrics**: Evaluates 6 key aspects of repository maintainability
- **Weighted Scoring**: Each metric has an appropriate weight reflecting its importance
- **Multiple Output Formats**: Supports both human-readable text and JSON formats
- **GitHub API Integration**: Fetches real-time data from GitHub
- **Production-Ready**: 90%+ test coverage with best practices

## Metrics

| Metric | Weight | Description |
|--------|--------|-------------|
| Documentation | 20% | Presence of README, LICENSE, CONTRIBUTING, CODE_OF_CONDUCT, CHANGELOG |
| Issue Management | 20% | Issue closure rate and open issue count |
| Commit Quality | 15% | Adherence to commit message conventions |
| Activity | 15% | Repository freshness based on recent commits |
| Community | 15% | Stars, forks, and contributor count |
| Branch Management | 15% | Number of branches (fewer is better) |

## Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Build from Source

```bash
git clone <repository-url>
cd repo-maintainability-index
mvn clean package
```

This will create an executable JAR file in the `target/` directory.

## Usage

### Basic Usage

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

### With GitHub Token (Recommended)

To avoid rate limiting, use a GitHub personal access token:

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --token YOUR_GITHUB_TOKEN
```

### JSON Output

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --format json
```

### Help

```bash
java -jar target/repo-maintainability-index-1.0.0.jar --help
java -jar target/repo-maintainability-index-1.0.0.jar analyze --help
```

## Example Output

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

▪ Commit Quality: 85.00/100 (weight: 15%)
  Evaluates commit message quality and conventions
  Details: Analyzed 50 commits: 42 (85.0%) follow conventions

...

───────────────────────────────────────────────────────────────
  Recommendation
───────────────────────────────────────────────────────────────

Good repository maintainability. Keep up the good work!

═══════════════════════════════════════════════════════════════
```

## Development

### Running Tests

```bash
mvn test
```

### Code Coverage

```bash
mvn clean test jacoco:report
```

View the coverage report at `target/site/jacoco/index.html`.

The project enforces 90% code coverage for both instructions and branches.

### Project Structure

```
src/
├── main/
│   ├── java/com/kaicode/rmi/
│   │   ├── cli/          # Command-line interface
│   │   ├── github/       # GitHub API client
│   │   ├── metrics/      # Metric calculators
│   │   ├── model/        # Data models
│   │   ├── service/      # Business logic
│   │   ├── util/         # Utilities
│   │   └── Main.java     # Application entry point
│   └── resources/
│       └── logback.xml   # Logging configuration
└── test/
    └── java/com/kaicode/rmi/
        └── ...           # Comprehensive test suite
```

## Architecture

The application follows clean architecture principles:

- **CLI Layer**: Handles command-line interface using Picocli
- **Service Layer**: Orchestrates business logic
- **Metrics Layer**: Implements individual metric calculations
- **GitHub Client**: Manages API interactions
- **Model Layer**: Defines domain objects

## Best Practices Implemented

- ✅ Builder pattern for complex objects
- ✅ Immutable models
- ✅ Dependency injection
- ✅ Interface-based design
- ✅ Comprehensive error handling
- ✅ Logging with SLF4J/Logback
- ✅ 90%+ test coverage
- ✅ Unit and integration tests
- ✅ Mock-based testing
- ✅ Parameterized tests

## Dependencies

- **Picocli**: Command-line interface framework
- **OkHttp**: HTTP client for GitHub API
- **Gson**: JSON parsing
- **SLF4J/Logback**: Logging
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework
- **AssertJ**: Fluent assertions

## License

This project is part of the Kaicode festival submission.

## Contributing

This is a festival submission project. For production use, consider:
- Adding more metrics (code quality, security, CI/CD)
- Implementing ChatGPT integration for AI-powered insights
- Adding caching for API responses
- Supporting more Git platforms (GitLab, Bitbucket)
- Creating a web interface

## Authors

Created for the Kaicode festival - Repository Maintainability Index challenge.
