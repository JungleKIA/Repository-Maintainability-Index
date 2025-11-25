# Repository Maintainability Index (RMI)

A production-level command-line tool for automated evaluation of GitHub repository quality and maintainability.

## 🚀 Quick Start Guide (First 5 Minutes)

### Quick Prerequisites Check
```bash
# 1. Check Java version
java -version

# Should show Java 17 or higher
# If not installed: https://adoptium.net/
```

### Installation
```bash
# Option 1: Maven (for developers)
git clone https://github.com/JungleKIA/Repository-Maintainability-Index.git
cd Repository-Maintainability-Index
mvn clean package

# Option 2: Direct JAR download (recommended for users)
curl -L https://github.com/JungleKIA/Repository-Maintainability-Index/releases/download/v1.0.2/repo-maintainability-index-1.0.1.jar -o rmi.jar
```

### Your First Analysis
```bash
# Basic repository analysis
java -jar target/repo-maintainability-index-1.0.1.jar analyze microsoft/vscode

# AI-powered analysis (requires API key)
OPENROUTER_API_KEY=your_api_key java -jar target/repo-maintainability-index-1.0.1.jar analyze microsoft/vscode --llm

# JSON output for automation
java -jar target/repo-maintainability-index-1.0.1.jar analyze microsoft/vscode --format json
```

### Example Output
You should see something like:
```
═══════════════════════════════════════════════════════════════
  Repository Maintainability Index Report
═══════════════════════════════════════════════════════════════

Repository: microsoft/vscode
Overall Score: 94.20/100 [██████████████░ EXCELLENT]

▪ Documentation: 100.00/100
▪ Commit Quality: 90.00/100
▪ Activity: 98.00/100
▪ Community: 95.00/100
▪ Issue Management: 88.00/100
```

**Need help?** See [QUICK_START.md](QUICK_START.md) for detailed setup or the full documentation below.

---

> **🚀 Quick Start**: New to RMI? Check out [QUICK_START.md](QUICK_START.md) for a complete step-by-step guide!

> **📊 Self-Analysis**: This repository practices what it preaches! Check [docs/REPOSITORY_IMPROVEMENT_SUMMARY.md](docs/REPOSITORY_IMPROVEMENT_SUMMARY.md) to see how we improved our own score from 53.45/100 to 69.45/100+ by following our tool's recommendations.

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
- **📚 Complete Documentation**: Full suite of documentation (CONTRIBUTING.md, CODE_OF_CONDUCT.md, LICENSE, CHANGELOG.md)
- **🚀 Quiet Mode**: `--quiet` flag suppresses progress logs while showing analysis results (perfect for scripting)

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

## 🐳 Docker Installation

For enterprise deployments, containerized installation is the preferred method.

### Prerequisites

- Docker Engine 20.10 or higher
- Docker Compose Plugin (optional, for multi-container setup)

### Option 1: Build from Source

```bash
# Clone and build Docker image
git clone <repository-url>
cd repo-maintainability-index

# Build Docker image
docker build -t rmi-app .

# Run container
docker run --rm -e GITHUB_TOKEN=your_token rmi-app analyze owner/repo
```

### Option 2: Use Pre-built Image (Recommended)

When available on GitHub Container Registry:

```bash
# Pull latest image
docker pull ghcr.io/junglekia/repository-maintainability-index:latest

# Run analysis
docker run --rm \
  -e GITHUB_TOKEN=your_token \
  -e OPENROUTER_API_KEY=your_llm_key \
  ghcr.io/junglekia/repository-maintainability-index:latest \
  analyze owner/repo --llm
```

### Option 3: Docker Compose (Development/Testing)

```bash
# Start with compose (includes volume mounts)
docker-compose up -d rmi

# Run analysis
docker-compose exec rmi analyze owner/repo --llm
```

### Environment Configuration for Docker

Create a `.env` file:

```bash
GITHUB_TOKEN=your_github_token_here
OPENROUTER_API_KEY=your_llm_key_here
LOG_LEVEL=INFO
```

Run with environment file:
```bash
docker run --rm --env-file .env rmi-app analyze owner/repo --llm
```

### Docker Launcher Scripts

Use provided scripts for easy Docker deployment:

```bash
# Build and run
./run-docker.sh build
./run-docker.sh run analyze owner/repo --llm

# Or directly
./run-docker.sh analyze owner/repo --llm
```

### Production Deployment

For production, use:

```yaml
# Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rmi-application
spec:
  replicas: 1
  selector:
    matchLabels:
      app: rmi
  template:
    metadata:
      labels:
        app: rmi
    spec:
      containers:
      - name: rmi
        image: ghcr.io/junglekia/repository-maintainability-index:latest
        env:
        - name: GITHUB_TOKEN
          valueFrom:
            secretKeyRef:
              name: rmi-secrets
              key: github-token
        ports:
        - containerPort: 8080
        securityContext:
          runAsNonRoot: true
          allowPrivilegeEscalation: false
```

### Docker Image Features

- ✅ **Multi-stage build** - Optimized image size
- ✅ **Security hardened** - Non-root user, minimal image
- ✅ **Health checks** - Automatic container monitoring
- ✅ **Resource limits** - CPU/memory constraints
- ✅ **UTF-8 support** - Proper Unicode handling
- ✅ **ARM64/AMD64** - Multi-platform support

## Environment Configuration

The application supports configuration via environment variables. You can set them directly or use a `.env` file in the project root.

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `GITHUB_TOKEN` | GitHub personal access token for higher API rate limits | None | Recommended |
| `OPENROUTER_API_KEY` | API key for OpenRouter LLM service | None | Required for LLM features |
| `OPENROUTER_MODEL` | LLM model to use for analysis | `openai/gpt-oss-20b:free` | Optional |
| `OPENAI_API_BASE` | OpenRouter API base URL | `https://openrouter.ai/api/v1` | Optional |

### Using .env File

Create a `.env` file in the project root:

```bash
# GitHub API token for authentication
# Get your token from: https://github.com/settings/tokens
# Required scopes: public_repo, repo:status
GITHUB_TOKEN=your_github_token_here

OPENROUTER_API_KEY=your_openrouter_api_key_here

# Optional: Custom model
OPENROUTER_MODEL=openai/gpt-oss-20b:free

# Optional: Custom API base (usually not needed)
OPENAI_API_BASE=https://openrouter.ai/api/v1
```

**⚠️ Security Warning:** Never commit `.env` files to version control. The project `.gitignore` already excludes `.env` files.

### Alternative: Set Environment Variables Directly

```bash
export GITHUB_TOKEN=your_github_token_here
export OPENROUTER_API_KEY=your_openrouter_api_key_here
```

## Usage

### 🪟 Windows & GitBash: Unicode/UTF-8 Support

**IMPORTANT**: For proper Unicode display (box-drawing characters like ═, ─, │, ┌, ┐, └, ┘), you **MUST** use one of these methods:

> **Why?** Java requires `-Dfile.encoding=UTF-8` to be set at JVM startup. This cannot be changed after the JVM starts. Additionally, Git Bash on Windows needs `chcp 65001` to display Unicode correctly.

#### ✅ Method 1: Use the provided launcher scripts (RECOMMENDED)

```bash
# Windows - Quick launcher
rmi.bat analyze owner/repo

# Windows - Full-featured launcher  
run-with-encoding.bat analyze owner/repo --llm

# Linux / macOS / Git Bash
./run-with-encoding.sh analyze owner/repo --llm
```

#### ✅ Method 2: Add `-Dfile.encoding=UTF-8` flag

```bash
# Windows Command Prompt
chcp 65001
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo

# Git Bash
export LANG=en_US.UTF-8
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo

# PowerShell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

**Why is this needed?** Java requires the `-Dfile.encoding=UTF-8` flag to be set at JVM startup for proper Unicode handling. The application cannot change this after the JVM has started. The provided scripts handle this automatically.

**❌ This will NOT work correctly:**
```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
# You'll see garbled characters like 'ΓòÉ' instead of '═'
```

**💡 Tip**: To avoid typing the flag every time, you can configure Git Bash to always use UTF-8:

**Quick setup (automatic):**
```bash
./setup-gitbash-utf8.sh
```

**Manual setup:** See [GITBASH_UTF8_SETUP.md](GITBASH_UTF8_SETUP.md) for detailed instructions.

### Basic Usage

**⚠️ IMPORTANT**: Always use `-Dfile.encoding=UTF-8` for proper Unicode display:

```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

Or use the quick launcher script:
```bash
rmi.bat analyze owner/repo
```

### With GitHub Token (Recommended)

To avoid rate limiting, use a GitHub personal access token. You can set it via environment variable or command line:

```bash
# Via environment variable (recommended) - DON'T FORGET -Dfile.encoding=UTF-8!
GITHUB_TOKEN=your_token java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo

# Via command line parameter
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --token YOUR_GITHUB_TOKEN

# Or use the script (handles encoding automatically)
rmi.bat analyze owner/repo --token YOUR_GITHUB_TOKEN
```

See [Environment Configuration](#environment-configuration) for details on setting up `.env` files.

### With AI Analysis (LLM)

Enable AI-powered deep analysis with LLM integration. Set your OpenRouter API key via environment variable or `.env` file:

```bash
# Using environment variable - DON'T FORGET -Dfile.encoding=UTF-8!
OPENROUTER_API_KEY=your_api_key_here java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm

# Or set OPENROUTER_API_KEY in your .env file and use the script
rmi.bat analyze owner/repo --llm
```

This provides:
- 📖 **README Analysis**: Clarity, completeness, and newcomer-friendliness scores
- 📝 **Commit Quality**: Analysis of commit message patterns and consistency
- 👥 **Community Health**: Responsiveness, helpfulness, and tone evaluation
- 💡 **AI Recommendations**: Prioritized suggestions with impact and confidence scores

⚠️ **Security Note**: OpenRouter automatically disables API keys exposed in public repositories. Always use environment variables or `.env` files (never commit them to git). See [Environment Configuration](#environment-configuration) and [SECURITY_BEST_PRACTICES.md](SECURITY_BEST_PRACTICES.md) for proper usage.

You can specify a custom model via environment variable or command line:

```bash
# Via environment variable
OPENROUTER_MODEL=openai/gpt-4 java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm

# Via command line parameter (overrides environment variable)
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm --model openai/gpt-4

# Or use the script
rmi.bat analyze owner/repo --llm --model openai/gpt-4
```

### Quiet Mode (Scripting)

For automation and scripting, use `--quiet` to suppress progress logs while keeping analysis results:

```bash
# Quiet mode - only shows final analysis, no logs
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --quiet

# With LLM analysis in quiet mode
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm --quiet
```

### JSON Output

```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --format json
```

### Help

```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar --help
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze --help
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

## Troubleshooting Unicode Display

### Problem: Garbled Characters in Git Bash/Windows

If you see garbled characters like `ΓòÉ`, `ΓöÇ`, `Γû¬` instead of box-drawing characters (`═`, `─`, `▪`), this is a Unicode encoding issue.

**The application automatically handles this**, but if you still experience issues:

#### Solution 1: Use Provided Scripts (Recommended)

```bash
# Windows
run-with-encoding.bat analyze owner/repo

# Git Bash / Linux / macOS
./run-with-encoding.sh analyze owner/repo
```

These scripts automatically configure UTF-8 encoding for your terminal.

#### Solution 2: Manual Configuration

**Git Bash:**
```bash
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

**Windows Command Prompt:**
```cmd
chcp 65001
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

**PowerShell:**
```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

#### Solution 3: Configure Git Bash Font

If characters still don't display correctly:

1. Right-click on Git Bash title bar → Options
2. Go to Text → Select...
3. Choose a Unicode-compatible font:
   - **Consolas** (recommended)
   - **Cascadia Code**
   - **Fira Code**
4. Click OK and restart Git Bash

### Common Issues

| Symptom | Cause | Solution |
|---------|-------|----------|
| `ΓòÉ` instead of `═` | UTF-8 bytes interpreted as Windows-1252 | Use provided scripts or set `chcp 65001` |
| Empty squares `□` | Font doesn't support Unicode | Change font to Consolas or Cascadia Code |
| Question marks `?` | Terminal doesn't support UTF-8 | Set `LANG=en_US.UTF-8` in Git Bash |
| Mixed correct/incorrect | Inconsistent encoding | Restart terminal after setting encoding |

### Verification

To verify Unicode support is working:

```bash
# Should display box-drawing characters correctly
java -jar target/repo-maintainability-index-1.0.0.jar --help
```

You should see:
- `═══` (double horizontal lines)
- `───` (single horizontal lines)  
- `▪` (bullet points)
- Emojis like 🤖 📖 💡

If you see these correctly, Unicode support is working!

## Documentation

### 📚 Essential Documentation

- **[CONTRIBUTING.md](CONTRIBUTING.md)** - How to contribute to this project
- **[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)** - Community standards and guidelines
- **[LICENSE](LICENSE)** - MIT License
- **[CHANGELOG.md](CHANGELOG.md)** - Version history and changes

### 📖 Additional Resources

- **[Quick Start Guide](QUICK_START.md)** - Get started in 5 minutes
- **[Security Best Practices](SECURITY_BEST_PRACTICES.md)** - Security guidelines
- **[LLM Features](LLM_FEATURES.md)** - AI-powered analysis documentation

### 🏗️ Technical Documentation

- **[Production Documentation](docs/)** - Complete production guides
  - [Production Readiness Summary](docs/PRODUCTION_READINESS_SUMMARY.md)
  - [Operations Runbook](docs/OPERATIONS_RUNBOOK.md)
  - [Deployment Guide](docs/DEPLOYMENT_GUIDE.md)
  - [API Specification](docs/API_SPECIFICATION.md)
- **[Architecture Documentation](docs/architecture/)** - System architecture
  - [C4 Architecture Diagrams](docs/architecture/C4_ARCHITECTURE.md)
  - [Architecture Decision Records (ADRs)](docs/architecture/adr/)
- **[Documentation Index](docs/INDEX.md)** - Complete documentation index

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
- **[Architecture Documentation](docs/architecture/C4_ARCHITECTURE.md)** - System design with C4 diagrams
- **[Architecture Decision Records](docs/architecture/adr/README.md)** - Key architectural decisions
- **[Implementation Notes](docs/IMPLEMENTATION_NOTES.md)** - What has been implemented
- **[Modernization Roadmap](docs/MODERNIZATION_ROADMAP.md)** - Implementation status and future work
- **[Testing Documentation](docs/TESTING_VERIFICATION.md)** - Test results and verification

### Quick Documentation Guide

| What You Want | Read This |
|---------------|-----------|
| Quick start guide | This README |
| What's been implemented? | [docs/IMPLEMENTATION_NOTES.md](docs/IMPLEMENTATION_NOTES.md) |
| How is it architected? | [docs/architecture/C4_ARCHITECTURE.md](docs/architecture/C4_ARCHITECTURE.md) |
| Why was X decision made? | [docs/architecture/adr/](docs/architecture/adr/) |
| Testing and verification | [docs/TESTING_VERIFICATION.md](docs/TESTING_VERIFICATION.md) |

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

See [docs/TESTING_VERIFICATION.md](docs/TESTING_VERIFICATION.md) for detailed verification results.

### ✅ LLM Integration Tested

**Real API Testing:** LLM integration tested with actual OpenRouter API key

- ✅ Tested on: facebook/react repository
- ✅ All LLM features working (README, Commit Quality, Community Health analysis)
- ✅ AI recommendations generated successfully
- ✅ API limits tracked correctly
- ✅ Graceful fallback functioning
- ✅ API keys handled securely (no storage, environment variables only)

### 🪟 UTF-8 Support for Windows

**Fixed:** Emoji display issues in Windows consoles

- ✅ Automatic UTF-8 configuration on startup
- ✅ Provided scripts: `run-analysis.bat` (Windows), `run-analysis.sh` (Linux/macOS)
- ✅ Tested on Git Bash, Command Prompt, PowerShell, Windows Terminal

If you see question marks (?) instead of emojis, try running with UTF-8 encoding:
```bash
# Windows Command Prompt
chcp 65001
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo

# Git Bash
export LANG=en_US.UTF-8
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo

# Or use the provided script
./run-analysis.bat analyze owner/repo  # Windows
./run-analysis.sh analyze owner/repo   # Linux/macOS
```

## Authors

Created for the Kaicode festival - Repository Maintainability Index challenge.
# GitHub Branch Protection activate
# GitHub Branch Protection activate
# Final CI/CD protection test
# Final test commit for status checks
# Status check generation test
# Enterprise branch protection test - CI/CD level Spring Boot/Apache/Google
