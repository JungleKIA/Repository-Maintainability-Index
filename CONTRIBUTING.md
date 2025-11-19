# Contributing to Repository Maintainability Index

Thank you for your interest in contributing to RMI! We welcome contributions from the community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Guidelines](#issue-guidelines)

---

## Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

---

## Getting Started

### Prerequisites

Before you begin, ensure you have:
- Java 17 or higher
- Maven 3.6 or higher
- Git
- A GitHub account

### Quick Setup

```bash
# Fork and clone the repository
git clone https://github.com/YOUR_USERNAME/Repository-Maintainability-Index.git
cd Repository-Maintainability-Index

# Build the project
mvn clean install

# Run tests
mvn test

# Run the application
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

---

## How to Contribute

### Ways to Contribute

1. **Report Bugs** - Found a bug? Open an issue
2. **Suggest Features** - Have an idea? We'd love to hear it
3. **Improve Documentation** - Help make our docs better
4. **Write Code** - Fix bugs or implement features
5. **Review Pull Requests** - Help review contributions
6. **Share Feedback** - Tell us how you're using RMI

### First Time Contributors

Look for issues labeled:
- `good first issue` - Easy issues for beginners
- `help wanted` - Issues where we need help
- `documentation` - Documentation improvements

---

## Development Setup

### 1. Fork and Clone

```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/Repository-Maintainability-Index.git
cd Repository-Maintainability-Index

# Add upstream remote
git remote add upstream https://github.com/JungleKIA/Repository-Maintainability-Index.git
```

### 2. Create a Branch

```bash
# Update your main branch
git checkout main
git pull upstream main

# Create a feature branch
git checkout -b feature/your-feature-name
```

### 3. Set Up Environment

```bash
# Copy environment template
cp .env.example .env

# Edit .env with your credentials
# GITHUB_TOKEN=your_token_here
# OPENROUTER_API_KEY=your_key_here (optional)
```

### 4. Build and Test

```bash
# Build the project
mvn clean package

# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## Coding Standards

### Java Style Guide

We follow **Google Java Style Guide** with some modifications:

#### Formatting
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: 120 characters maximum
- **Braces**: Required for all control structures

#### Naming Conventions
```java
// Classes: PascalCase
public class MetricCalculator { }

// Methods: camelCase
public MetricResult calculateScore() { }

// Variables: camelCase
private int totalScore;

// Constants: UPPER_SNAKE_CASE
private static final int MAX_RETRIES = 3;

// Packages: lowercase
package com.kaicode.rmi.metrics;
```

#### Best Practices

1. **Single Responsibility Principle**
   ```java
   // ✅ Good: Each class has one responsibility
   public class DocumentationMetric implements MetricCalculator {
       @Override
       public MetricResult calculate(RepositoryInfo repoInfo) {
           // Only documentation scoring logic
       }
   }
   
   // ❌ Bad: Class doing too much
   public class MetricCalculator {
       public double calculateDocumentation() { }
       public double calculateCommits() { }
       public double generateReport() { }
   }
   ```

2. **Immutable Models**
   ```java
   // ✅ Good: Immutable with builder
   public class MetricResult {
       private final String name;
       private final double score;
       
       private MetricResult(Builder builder) {
           this.name = builder.name;
           this.score = builder.score;
       }
       
       public static Builder builder() {
           return new Builder();
       }
   }
   
   // ❌ Bad: Mutable object
   public class MetricResult {
       public String name;
       public double score;
   }
   ```

3. **Null Safety**
   ```java
   // ✅ Good: Validate inputs
   public Builder name(String name) {
       this.name = Objects.requireNonNull(name, "name cannot be null");
       return this;
   }
   
   // ❌ Bad: No validation
   public Builder name(String name) {
       this.name = name;
       return this;
   }
   ```

4. **Defensive Copying**
   ```java
   // ✅ Good: Return unmodifiable collection
   public List<String> getFiles() {
       return Collections.unmodifiableList(files);
   }
   
   // ❌ Bad: Expose internal mutable state
   public List<String> getFiles() {
       return files;
   }
   ```

### Code Quality Checks

Before submitting, ensure:
```bash
# Run SpotBugs
mvn spotbugs:check

# Run Checkstyle
mvn checkstyle:check

# Verify test coverage (90% instructions, 84% branches minimum)
mvn clean verify
```

---

## Testing Guidelines

### Test Structure

We use **JUnit 5** with **Mockito** and **AssertJ**:

```java
@ExtendWith(MockitoExtension.class)
class MaintainabilityServiceTest {
    
    @Mock
    private GitHubClient githubClient;
    
    @InjectMocks
    private MaintainabilityService service;
    
    @Test
    void shouldCalculateCorrectOverallScoreWhenAllMetricsProvided() {
        // Arrange
        RepositoryInfo repoInfo = createTestRepositoryInfo();
        when(githubClient.fetchRepository("owner", "repo")).thenReturn(repoInfo);
        
        // Act
        MaintainabilityReport report = service.analyze("owner", "repo");
        
        // Assert
        assertThat(report.getOverallScore())
            .isGreaterThan(0)
            .isLessThanOrEqualTo(100);
        verify(githubClient).fetchRepository("owner", "repo");
    }
}
```

### Test Naming Convention

Use descriptive test names following the pattern:
```
should[ExpectedBehavior]When[StateUnderTest]
```

Examples:
```java
void shouldReturnExcellentRatingWhenScoreAbove90()
void shouldThrowExceptionWhenRepositoryNotFound()
void shouldHandleNullValuesGracefully()
```

### Test Categories

1. **Unit Tests** - Test individual components in isolation
   - File: `*Test.java`
   - Example: `DocumentationMetricTest.java`

2. **Edge Case Tests** - Test boundary conditions
   - File: `*EdgeCaseTest.java`
   - Example: `MetricCalculatorsEdgeCaseTest.java`

3. **Branch Coverage Tests** - Ensure all branches tested
   - File: `*BranchCoverageTest.java`
   - Example: `ModelBranchCoverageTest.java`

### Test Coverage Requirements

- **Instruction Coverage**: Minimum 90%
- **Branch Coverage**: Minimum 84%
- **All public methods**: Must be tested
- **Edge cases**: Must have dedicated tests

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=DocumentationMetricTest

# Run with coverage report
mvn clean test jacoco:report

# Run only fast tests (exclude integration)
mvn test -Dgroups="!integration"
```

---

## Pull Request Process

### 1. Before Creating PR

Ensure your changes:
- [ ] Follow coding standards
- [ ] Include tests (90%+ coverage)
- [ ] Pass all existing tests
- [ ] Update documentation
- [ ] Add entry to CHANGELOG.md (if applicable)

### 2. Create Pull Request

```bash
# Commit your changes
git add .
git commit -m "feat: add new metric calculator"

# Push to your fork
git push origin feature/your-feature-name

# Create PR on GitHub
```

### 3. PR Title and Description

**Title Format:**
```
<type>: <short description>

Types: feat, fix, docs, style, refactor, test, chore
```

Examples:
- `feat: add documentation completeness metric`
- `fix: handle null pointer in commit analyzer`
- `docs: update README with new examples`

**Description Template:**
```markdown
## What does this PR do?
Brief description of changes

## Why is this needed?
Explain the motivation

## Related Issues
Closes #123

## Testing
- [ ] Unit tests added
- [ ] Integration tests added
- [ ] Manual testing performed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review performed
- [ ] Documentation updated
- [ ] Tests pass locally
- [ ] CHANGELOG.md updated
```

### 4. Review Process

1. **Automated Checks** - Must pass:
   - Build and compilation
   - All tests (unit + integration)
   - Code coverage (≥90% instructions, ≥84% branches)
   - Static analysis (SpotBugs, Checkstyle)
   - Security scanning (OWASP)

2. **Code Review** - At least one approval required:
   - Code quality
   - Test coverage
   - Documentation
   - Performance implications

3. **Feedback** - Address review comments:
   - Make requested changes
   - Push updates to same branch
   - Re-request review

4. **Merge** - After approval:
   - Squash and merge (preferred)
   - Include issue number in merge commit

---

## Issue Guidelines

### Creating Issues

#### Bug Reports

Use the bug report template:

```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Run command '...'
2. See error

**Expected behavior**
What you expected to happen.

**Actual behavior**
What actually happened.

**Environment:**
- OS: [e.g., Windows 10, Ubuntu 22.04]
- Java version: [e.g., Java 17]
- RMI version: [e.g., 1.0.0]

**Logs**
```
paste relevant logs here
```

**Additional context**
Any other relevant information.
```

#### Feature Requests

Use the feature request template:

```markdown
**Is your feature request related to a problem?**
A clear description of the problem.

**Describe the solution you'd like**
What you want to happen.

**Describe alternatives you've considered**
Other solutions you've thought about.

**Additional context**
Any other relevant information.
```

### Issue Labels

We use the following labels:

| Label | Description |
|-------|-------------|
| `bug` | Something isn't working |
| `enhancement` | New feature or request |
| `documentation` | Documentation improvements |
| `good first issue` | Good for newcomers |
| `help wanted` | Extra attention needed |
| `question` | Further information requested |
| `duplicate` | Duplicate issue |
| `wontfix` | This will not be worked on |
| `priority: high` | High priority issue |
| `priority: medium` | Medium priority |
| `priority: low` | Low priority |

---

## Development Workflow

### Branch Strategy

- `main` - Production-ready code
- `develop` - Development branch
- `feature/*` - Feature branches
- `fix/*` - Bug fix branches
- `docs/*` - Documentation branches

### Commit Message Convention

We follow **Conventional Commits**:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting)
- `refactor` - Code refactoring
- `test` - Adding tests
- `chore` - Maintenance tasks

**Examples:**
```
feat(metrics): add code coverage metric

Add new metric to evaluate test coverage percentage.
Includes integration with JaCoCo and Cobertura.

Closes #45

---

fix(github): handle rate limit errors gracefully

Previously, rate limit errors caused crashes.
Now, the application waits and retries.

Fixes #67
```

### Release Process

1. Update version in `pom.xml`
2. Update `CHANGELOG.md`
3. Create release branch
4. Tag release: `git tag -a v1.0.0 -m "Release 1.0.0"`
5. Push tag: `git push origin v1.0.0`
6. GitHub Actions creates release automatically

---

## Architecture Guidelines

### Adding New Metrics

To add a new metric:

1. **Create metric class**:
   ```java
   package com.kaicode.rmi.metrics;
   
   public class NewMetric implements MetricCalculator {
       @Override
       public MetricResult calculate(RepositoryInfo repositoryInfo) {
           // Implementation
       }
   }
   ```

2. **Register in service**:
   ```java
   private final List<MetricCalculator> calculators = Arrays.asList(
       new DocumentationMetric(),
       new NewMetric(), // Add here
       // ...
   );
   ```

3. **Add tests**:
   ```java
   class NewMetricTest {
       @Test
       void shouldCalculateCorrectScore() {
           // Test implementation
       }
   }
   ```

4. **Update documentation**:
   - Add to README.md metrics table
   - Document weight and calculation logic
   - Add examples

### Design Principles

Follow these principles:

1. **SOLID Principles**
   - Single Responsibility
   - Open/Closed
   - Liskov Substitution
   - Interface Segregation
   - Dependency Inversion

2. **Design Patterns**
   - Strategy (MetricCalculator)
   - Builder (Models)
   - Factory (optional)

3. **Clean Code**
   - Meaningful names
   - Small functions
   - DRY (Don't Repeat Yourself)
   - Clear intent

---

## Getting Help

### Resources

- **Documentation**: [docs/](docs/)
- **Architecture**: [docs/architecture/](docs/architecture/)
- **API Reference**: [docs/API_SPECIFICATION.md](docs/API_SPECIFICATION.md)
- **Operations**: [docs/OPERATIONS_RUNBOOK.md](docs/OPERATIONS_RUNBOOK.md)

### Communication

- **GitHub Issues**: For bugs and feature requests
- **GitHub Discussions**: For questions and ideas
- **Pull Requests**: For code contributions

### Contact

- **Maintainers**: Listed in [README.md](README.md)
- **Security Issues**: See [SECURITY.md](SECURITY.md)

---

## Recognition

We appreciate all contributions! Contributors will be:
- Listed in release notes
- Mentioned in CHANGELOG.md
- Added to contributors list

---

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (see [LICENSE](LICENSE)).

---

**Thank you for contributing to RMI! 🎉**
