# Repository Maintainability Index (RMI)

A production-level command-line tool for automated evaluation of GitHub repository quality and maintainability.

## Overview

The Repository Maintainability Index tool analyzes GitHub repositories and provides a comprehensive assessment of their quality based on multiple metrics including documentation, commit quality, activity, issue management, community engagement, and branch management.

## Features

- **Comprehensive Metrics**: Evaluates 6 key aspects of repository maintainability
- **Weighted Scoring**: Each metric has an appropriate weight reflecting its importance
- **🤖 AI-Powered Analysis**: Optional LLM integration for deep insights into README quality, commit patterns, and community health
- **Smart Recommendations**: AI-generated actionable recommendations with impact and confidence scores
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

### 🪟 Windows Users: UTF-8 Support

If you see question marks (?) instead of emojis in Windows:

**Quick Fix (Git Bash / Command Prompt):**
```bash
# Git Bash
export LANG=en_US.UTF-8
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo

# Command Prompt
chcp 65001
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

**Or use the provided scripts:**
```bash
# Windows
run-analysis.bat analyze owner/repo --llm

# Linux / macOS / Git Bash
./run-analysis.sh analyze owner/repo --llm
```

See [WINDOWS_SETUP.md](WINDOWS_SETUP.md) for detailed Windows configuration guide.

### Basic Usage

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

### With GitHub Token (Recommended)

To avoid rate limiting, use a GitHub personal access token:

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --token YOUR_GITHUB_TOKEN
```

### With AI Analysis (LLM)

Enable AI-powered deep analysis with LLM integration:

```bash
# IMPORTANT: Never commit your API key to git!
# See SECURITY_BEST_PRACTICES.md for details
export OPENROUTER_API_KEY=your_api_key_here
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm
```

This provides:
- 📖 **README Analysis**: Clarity, completeness, and newcomer-friendliness scores
- 📝 **Commit Quality**: Analysis of commit message patterns and consistency
- 👥 **Community Health**: Responsiveness, helpfulness, and tone evaluation
- 💡 **AI Recommendations**: Prioritized suggestions with impact and confidence scores

⚠️ **Security Note**: OpenRouter automatically disables API keys exposed in public repositories. Always use environment variables. See [SECURITY_BEST_PRACTICES.md](SECURITY_BEST_PRACTICES.md) for proper usage.

You can specify a custom model:

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm --model openai/gpt-4
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

## 📚 Documentation

For detailed technical documentation, see:

- **[Documentation Index](docs/README.md)** - Complete documentation guide
- **[Enterprise Assessment](ENTERPRISE_ASSESSMENT.md)** - ROI analysis of enterprise patterns (Russian)
- **[Ответ на Вопрос](ОТВЕТ_НА_ВОПРОС.md)** - Краткий ответ: нужна ли enterprise-модернизация? (Russian)
- **[Architecture Documentation](docs/architecture/C4_ARCHITECTURE.md)** - System design with C4 diagrams
- **[Architecture Decision Records](docs/architecture/adr/README.md)** - Key architectural decisions
- **[Modernization Roadmap](docs/MODERNIZATION_ROADMAP.md)** - Implementation status and future work

### Key Documents Summary

| What You Want | Read This |
|---------------|-----------|
| Is enterprise modernization worth it? | [ОТВЕТ_НА_ВОПРОС.md](ОТВЕТ_НА_ВОПРОС.md) (Russian) |
| Full ROI analysis | [ENTERPRISE_ASSESSMENT.md](ENTERPRISE_ASSESSMENT.md) (Russian) |
| What's been implemented? | [docs/IMPLEMENTATION_NOTES.md](docs/IMPLEMENTATION_NOTES.md) |
| How is it architected? | [docs/architecture/C4_ARCHITECTURE.md](docs/architecture/C4_ARCHITECTURE.md) |
| Why was X decision made? | [docs/architecture/adr/](docs/architecture/adr/) |

## Quality & Security

This project implements production-ready engineering practices:

- ✅ **90%+ Test Coverage** - Comprehensive unit and integration tests
- ✅ **Security Scanning** - OWASP Dependency-Check, Trivy
- ✅ **SBOM Generation** - CycloneDX Software Bill of Materials
- ✅ **Code Quality Gates** - SpotBugs, Checkstyle
- ✅ **CI/CD Pipeline** - Automated testing and quality checks
- ✅ **Architecture Documentation** - C4 diagrams and ADRs

See [CI/CD Pipeline](.github/workflows/ci.yml) for details.

## ✅ Verification Status

**Last Verified:** 2024-11-07  
**Status:** ✅ ALL CHECKS PASSED

- ✅ Build: SUCCESS (mvn clean package)
- ✅ Tests: 216/216 passed
- ✅ Coverage: 90%+ instructions, 85%+ branches
- ✅ Application runs correctly
- ✅ LLM integration functional (with graceful fallback)
- ✅ API keys handled securely (environment variables only)
- ✅ No hardcoded tokens in source code

**Quick Verification:**
```bash
# Build and verify
mvn clean package
java -jar target/repo-maintainability-index-1.0.0.jar --help

# Run analysis
java -jar target/repo-maintainability-index-1.0.0.jar analyze picocli/picocli

# With LLM (requires OPENROUTER_API_KEY env var)
OPENROUTER_API_KEY="your-key" java -jar target/repo-maintainability-index-1.0.0.jar analyze picocli/picocli --llm
```

See [VERIFICATION_SUMMARY.md](VERIFICATION_SUMMARY.md) or [ПРОВЕРКА_ЗАВЕРШЕНА.md](ПРОВЕРКА_ЗАВЕРШЕНА.md) (Russian) for detailed verification results.

### ✅ LLM Integration Tested

**Real API Testing:** LLM integration tested with actual OpenRouter API key on 2024-11-07

- ✅ Tested on: facebook/react repository
- ✅ All LLM features working (README, Commit Quality, Community Health analysis)
- ✅ AI recommendations generated successfully
- ✅ API limits tracked correctly (3/50 requests used)
- ✅ Graceful fallback functioning
- ✅ API keys handled securely (no storage, environment variables only)

See [LLM_TESTING_RESULTS.md](LLM_TESTING_RESULTS.md) for complete LLM testing documentation.

### 🪟 UTF-8 Support for Windows

**Fixed:** Emoji display issues in Windows consoles (2024-11-08)

- ✅ Automatic UTF-8 configuration on startup
- ✅ Provided scripts: `run-analysis.bat` (Windows), `run-analysis.sh` (Linux/macOS)
- ✅ Detailed setup guide for all Windows terminals
- ✅ Tested on Git Bash, Command Prompt, PowerShell, Windows Terminal

If you see question marks (?) instead of emojis, see [WINDOWS_SETUP.md](WINDOWS_SETUP.md) or [UTF8_FIX_SUMMARY.md](UTF8_FIX_SUMMARY.md).

## Authors

Created for the Kaicode festival - Repository Maintainability Index challenge.
