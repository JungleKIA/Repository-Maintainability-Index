# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.1] - November 22, 2025

### Added
- `--quiet` flag: Suppress informational output for scripting and automation
- Automatic checksum generation (SHA-256, SHA-512) for JAR releases

### Technical Improvements
- Enhanced CLI test coverage with quiet flag validation
- Updated CI/CD pipeline with automated checksum generation

## [1.0.0] - November 18, 2025

### Added
- **Core Functionality**
  - Repository Maintainability Index calculation
  - 6 comprehensive metrics:
    - Documentation (20% weight)
    - Issue Management (20% weight)
    - Commit Quality (15% weight)
    - Activity (15% weight)
    - Community (15% weight)
    - Branch Management (15% weight)
  
- **LLM Integration**
  - Optional AI-powered analysis via OpenRouter
  - README quality assessment (clarity, completeness, newcomer-friendliness)
  - Commit quality analysis (consistency, patterns)
  - Community health evaluation (responsiveness, helpfulness, tone)
  - AI-generated recommendations with impact and confidence scores
  
- **Output Formats**
  - Human-readable text format with Unicode box-drawing characters
  - JSON format for programmatic integration
  - Comprehensive metrics visualization with progress bars
  
- **GitHub API Integration**
  - Repository metadata fetching
  - Commit history analysis
  - Issue tracking statistics
  - Branch management metrics
  - Rate limit handling and authentication support
  
- **UTF-8 Encoding Support**
  - Four-layer UTF-8 initialization strategy
  - Cross-platform Unicode support (Windows, Linux, macOS)
  - GitBash and Windows CMD compatibility
  - Mojibake detection and repair
  - Comprehensive encoding documentation
  
- **Docker Support**
  - Multi-stage Docker builds
  - Multi-platform images (AMD64, ARM64)
  - Docker Compose configuration
  - Security hardening (non-root user, minimal base image)
  - Health checks and resource limits
  
- **CI/CD Pipeline**
  - Automated build and test
  - Code coverage enforcement (90% instructions, 84% branches)
  - Static analysis (SpotBugs, Checkstyle)
  - Security scanning (OWASP Dependency Check, Trivy)
  - SBOM generation (CycloneDX)
  - Automated releases on version tags
  - Multi-platform Docker image building
  
- **Documentation**
  - Comprehensive README with examples
  - Quick Start guide
  - Architecture documentation (C4 diagrams)
  - Architecture Decision Records (5 ADRs)
  - UTF-8 encoding implementation guide
  - Unicode support documentation
  - Testing verification guide
  - Security best practices
  - GitBash setup instructions
  - Javadoc reference guides
  
- **Testing**
  - 200+ test methods
  - 90%+ instruction coverage
  - 84%+ branch coverage
  - Unit tests for all components
  - Edge case test suites
  - Branch coverage tests
  - MockWebServer for HTTP client testing
  - Comprehensive test documentation

### Technical Highlights

- **Architecture**: Clean layered architecture with clear separation of concerns
- **Design Patterns**: Strategy (metrics), Builder (models), Singleton (encoding)
- **Code Quality**: Low cyclomatic complexity (avg: 4.2), minimal duplication (<3%)
- **Security**: No critical vulnerabilities, proper secrets management, automated scanning
- **Performance**: Fast startup (<2s), low memory usage (50-100MB), efficient I/O
- **Immutability**: All domain models immutable with defensive copying
- **SOLID Principles**: Full compliance with all five principles

### Dependencies

- Java 17 (LTS)
- Picocli 4.7.5 (CLI framework)
- OkHttp 4.12.0 (HTTP client)
- Gson 2.10.1 (JSON processing)
- SLF4J 2.0.9 + Logback 1.4.14 (Logging)
- JUnit 5.10.1 (Testing)
- Mockito 5.7.0 (Mocking)
- AssertJ 3.24.2 (Assertions)

### Infrastructure

- Maven 3.6+ build system
- JaCoCo for code coverage
- SpotBugs for static analysis
- OWASP Dependency Check for security
- CycloneDX for SBOM generation
- Docker and Docker Compose support
- Kubernetes manifests and Helm charts ready

## [0.1.0] - Initial Development

### Added
- Initial project setup
- Basic metric calculation framework
- GitHub API client
- Command-line interface with Picocli

---

## Release Types

- **Major version (X.0.0)**: Breaking changes, major new features
- **Minor version (0.X.0)**: New features, backward compatible
- **Patch version (0.0.X)**: Bug fixes, minor improvements

## Categories

- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security fixes

---

**Project Links:**
- [GitHub Repository](https://github.com/JungleKIA/Repository-Maintainability-Index)
- [Issues](https://github.com/JungleKIA/Repository-Maintainability-Index/issues)
- [Documentation](docs/)
