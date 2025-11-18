# Deployment Guide: Repository Maintainability Index

**Version**: 1.0.0  
**Last Updated**: January 17, 2025  
**Target Audience**: DevOps Engineers, System Administrators, Developers

---

## Table of Contents

1. [Overview](#overview)
2. [System Requirements](#system-requirements)
3. [Pre-deployment Checklist](#pre-deployment-checklist)
4. [Installation Methods](#installation-methods)
5. [Configuration](#configuration)
6. [Verification](#verification)
7. [Integration Scenarios](#integration-scenarios)
8. [Troubleshooting](#troubleshooting)
9. [Uninstallation](#uninstallation)

---

## Overview

Repository Maintainability Index (RMI) is a **command-line tool** distributed as an executable JAR file. Deployment is straightforward and requires minimal infrastructure.

### Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│               User Environment                          │
│                                                         │
│  ┌────────────────────────────────────────────┐        │
│  │  Java Runtime (JRE 17+)                    │        │
│  └──────────────────┬─────────────────────────┘        │
│                     │                                   │
│  ┌──────────────────▼─────────────────────────┐        │
│  │  RMI JAR (repo-maintainability-index.jar)  │        │
│  └──────────────────┬─────────────────────────┘        │
│                     │                                   │
│  ┌──────────────────▼─────────────────────────┐        │
│  │  Configuration (.env or env vars)          │        │
│  │  - GITHUB_TOKEN                             │        │
│  │  - OPENROUTER_API_KEY (optional)            │        │
│  └─────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────┘
                      │
                      │ HTTPS/REST
                      ▼
┌─────────────────────────────────────────────────────────┐
│           External Services                             │
│  - GitHub API (api.github.com)                          │
│  - OpenRouter API (openrouter.ai) [optional]            │
└─────────────────────────────────────────────────────────┘
```

---

## System Requirements

### Minimum Requirements

| Component | Requirement | Notes |
|-----------|-------------|-------|
| **Operating System** | Linux, macOS, Windows | Any OS with Java support |
| **Java Runtime** | JRE/JDK 17 or higher | LTS version recommended |
| **Memory** | 512 MB RAM | 256 MB heap + system overhead |
| **Disk Space** | 100 MB | 50 MB for JAR + 50 MB temp space |
| **Network** | Internet connectivity | HTTPS access to GitHub API |
| **CPU** | 1 core | Single-threaded application |

### Recommended Requirements

| Component | Recommendation | Reason |
|-----------|----------------|--------|
| **Java Runtime** | OpenJDK 17 LTS | Stability and long-term support |
| **Memory** | 1 GB RAM | Better performance for large repos |
| **Network** | Low-latency connection | Faster API responses |

### Network Requirements

#### Required Endpoints:

| Endpoint | Port | Protocol | Purpose |
|----------|------|----------|---------|
| `api.github.com` | 443 | HTTPS | GitHub API access |
| `github.com` | 443 | HTTPS | Repository metadata |

#### Optional Endpoints (for LLM features):

| Endpoint | Port | Protocol | Purpose |
|----------|------|----------|---------|
| `openrouter.ai` | 443 | HTTPS | LLM API access |

#### Firewall Rules:

```bash
# Outbound HTTPS to GitHub (required)
allow outbound tcp to api.github.com port 443
allow outbound tcp to github.com port 443

# Outbound HTTPS to OpenRouter (optional)
allow outbound tcp to openrouter.ai port 443
```

---

## Pre-deployment Checklist

### ✅ Prerequisites Verification

```bash
# 1. Check Java version (must be 17+)
$ java -version
openjdk version "17.0.9" 2023-10-17
OpenJDK Runtime Environment (build 17.0.9+9-Ubuntu-122.04)

# 2. Verify network connectivity
$ curl -I https://api.github.com
HTTP/2 200

# 3. Check available disk space
$ df -h /opt  # or your target directory
Filesystem      Size  Used Avail Use% Mounted on
/dev/sda1       20G   8G   12G  40% /

# 4. Verify memory availability
$ free -h
              total        used        free      shared  buff/cache   available
Mem:           2.0Gi       500Mi       1.0Gi       10Mi        500Mi       1.4Gi
```

### 📋 Required Credentials

- [ ] GitHub Personal Access Token (for authenticated access)
  - Scopes: `public_repo`, `repo:status`
  - Generate at: https://github.com/settings/tokens
  
- [ ] OpenRouter API Key (optional, for LLM features)
  - Sign up at: https://openrouter.ai/
  - Get key from: https://openrouter.ai/keys

---

## Installation Methods

### Method 1: Download Pre-built JAR (Recommended)

#### Step 1: Download

```bash
# From GitHub Releases
$ wget https://github.com/YOUR-ORG/repo-maintainability-index/releases/download/v1.0.0/repo-maintainability-index-1.0.0.jar

# Or using curl
$ curl -LO https://github.com/YOUR-ORG/repo-maintainability-index/releases/download/v1.0.0/repo-maintainability-index-1.0.0.jar
```

#### Step 2: Verify Checksum (Recommended)

```bash
# Download checksum file
$ wget https://github.com/YOUR-ORG/repo-maintainability-index/releases/download/v1.0.0/repo-maintainability-index-1.0.0.jar.sha256

# Verify
$ sha256sum -c repo-maintainability-index-1.0.0.jar.sha256
repo-maintainability-index-1.0.0.jar: OK
```

#### Step 3: Install

```bash
# Create installation directory
$ sudo mkdir -p /opt/rmi
$ sudo mv repo-maintainability-index-1.0.0.jar /opt/rmi/

# Create symlink for easier access
$ sudo ln -s /opt/rmi/repo-maintainability-index-1.0.0.jar /usr/local/bin/rmi.jar

# Create wrapper script
$ sudo tee /usr/local/bin/rmi > /dev/null <<'EOF'
#!/bin/bash
exec java -Dfile.encoding=UTF-8 -jar /opt/rmi/repo-maintainability-index-1.0.0.jar "$@"
EOF

$ sudo chmod +x /usr/local/bin/rmi
```

#### Step 4: Verify Installation

```bash
$ rmi --version
Repository Maintainability Index CLI 1.0.0

$ rmi --help
Usage: rmi [-hV] [COMMAND]
...
```

### Method 2: Build from Source

#### Prerequisites:
- Maven 3.6+
- Git

#### Steps:

```bash
# 1. Clone repository
$ git clone https://github.com/YOUR-ORG/repo-maintainability-index.git
$ cd repo-maintainability-index

# 2. Build with Maven
$ mvn clean package -DskipTests

# 3. JAR will be in target/
$ ls -lh target/repo-maintainability-index-1.0.0.jar
-rw-rw-r-- 1 user user 15M Nov 17 12:00 target/repo-maintainability-index-1.0.0.jar

# 4. Install (same as Method 1, Step 3)
$ sudo mkdir -p /opt/rmi
$ sudo cp target/repo-maintainability-index-1.0.0.jar /opt/rmi/
# ... continue with symlink and wrapper script
```

### Method 3: Docker Container (Future)

**Note**: Docker support is planned but not yet implemented. Proposed usage:

```bash
# Pull image (future)
$ docker pull ghcr.io/your-org/rmi:1.0.0

# Run analysis (future)
$ docker run --rm \
  -e GITHUB_TOKEN=$GITHUB_TOKEN \
  ghcr.io/your-org/rmi:1.0.0 \
  analyze facebook/react
```

---

## Configuration

### Environment Variables Setup

#### Option 1: System-wide Environment Variables

```bash
# For bash/zsh - add to ~/.bashrc or ~/.zshrc
export GITHUB_TOKEN="ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
export OPENROUTER_API_KEY="sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
export OPENROUTER_MODEL="openai/gpt-4"

# Apply changes
$ source ~/.bashrc
```

#### Option 2: .env File (Project-specific)

```bash
# Create .env file in your working directory
$ cat > .env <<'EOF'
# GitHub Configuration
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# LLM Configuration (optional)
OPENROUTER_API_KEY=sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
OPENROUTER_MODEL=openai/gpt-4
OPENAI_API_BASE=https://openrouter.ai/api/v1
EOF

# Secure the file (important!)
$ chmod 600 .env
```

#### Option 3: Command-line Arguments

```bash
# Pass token directly (not recommended for security)
$ rmi analyze facebook/react --token ghp_xxxxxxxxxxxx
```

### Security Best Practices

#### ✅ DO:
- Store tokens in environment variables
- Use `.env` files with restricted permissions (chmod 600)
- Use secret management systems (Vault, AWS Secrets Manager)
- Rotate tokens regularly
- Use fine-grained tokens with minimal scopes

#### ❌ DON'T:
- Commit `.env` files to version control
- Share tokens in plain text
- Log tokens in application output
- Store tokens in script files
- Use root account tokens

### Logging Configuration

#### Default Configuration

RMI uses Logback for logging. Default configuration:
- Console output: INFO level
- Format: Text with timestamps
- UTF-8 encoding

#### Custom Logging (Optional)

```bash
# Create custom logback.xml
$ cat > logback-custom.xml <<'EOF'
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>/var/log/rmi/application.log</file>
        <encoder>
            <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} %-5level - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE" />
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
EOF

# Use custom configuration
$ java -Dlogback.configurationFile=logback-custom.xml \
  -jar /opt/rmi/repo-maintainability-index-1.0.0.jar analyze facebook/react
```

---

## Verification

### Post-installation Tests

#### Test 1: Version Check

```bash
$ rmi --version
Expected output: Repository Maintainability Index CLI 1.0.0
```

#### Test 2: Help Display

```bash
$ rmi --help
Expected: Help text with commands and options
```

#### Test 3: Basic Analysis (Without Token)

```bash
$ rmi analyze picocli/picocli

Expected output:
═══════════════════════════════════════════════
  Repository Maintainability Index Report
═══════════════════════════════════════════════
Repository: picocli/picocli
Overall Score: 85.00/100
...
```

#### Test 4: Analysis with Token

```bash
$ rmi analyze facebook/react --token $GITHUB_TOKEN

Expected: Successful analysis with higher rate limit
```

#### Test 5: JSON Output

```bash
$ rmi analyze facebook/react --format json | jq '.overallScore'

Expected: Numeric score (e.g., 87.5)
```

#### Test 6: LLM Analysis (if configured)

```bash
$ rmi analyze facebook/react --llm

Expected: Analysis with LLM insights section
```

### Health Check Script

```bash
#!/bin/bash
# health-check.sh - Verify RMI installation

echo "=== RMI Health Check ==="

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found"
    exit 1
fi
echo "✅ Java: $(java -version 2>&1 | head -n1)"

# Check RMI
if ! command -v rmi &> /dev/null; then
    echo "❌ RMI not found in PATH"
    exit 1
fi
echo "✅ RMI: $(rmi --version)"

# Check network
if ! curl -sf https://api.github.com/zen &> /dev/null; then
    echo "❌ Cannot reach GitHub API"
    exit 1
fi
echo "✅ GitHub API: Reachable"

# Check token (if set)
if [ -n "$GITHUB_TOKEN" ]; then
    echo "✅ GitHub token: Configured"
else
    echo "⚠️  GitHub token: Not configured (rate limits apply)"
fi

# Test analysis
if rmi analyze picocli/picocli > /dev/null 2>&1; then
    echo "✅ Test analysis: Successful"
else
    echo "❌ Test analysis: Failed"
    exit 1
fi

echo "=== All checks passed ==="
```

---

## Integration Scenarios

### Scenario 1: CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/quality-check.yml
name: Repository Quality Check

on:
  pull_request:
  push:
    branches: [main]

jobs:
  quality-check:
    runs-on: ubuntu-latest
    
    steps:
      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Download RMI
        run: |
          wget https://github.com/YOUR-ORG/rmi/releases/download/v1.0.0/repo-maintainability-index-1.0.0.jar
          chmod +x repo-maintainability-index-1.0.0.jar
      
      - name: Analyze Repository
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          java -jar repo-maintainability-index-1.0.0.jar \
            analyze ${{ github.repository }} \
            --format json > report.json
      
      - name: Check Quality Gate
        run: |
          SCORE=$(jq -r '.overallScore' report.json)
          echo "Quality Score: $SCORE"
          if (( $(echo "$SCORE < 70" | bc -l) )); then
            echo "❌ Quality gate failed: score $SCORE < 70"
            exit 1
          fi
          echo "✅ Quality gate passed"
      
      - name: Upload Report
        uses: actions/upload-artifact@v4
        with:
          name: quality-report
          path: report.json
```

### Scenario 2: GitLab CI/CD

```yaml
# .gitlab-ci.yml
quality_check:
  stage: test
  image: openjdk:17-jdk-slim
  
  before_script:
    - apt-get update && apt-get install -y wget
    - wget https://github.com/YOUR-ORG/rmi/releases/download/v1.0.0/repo-maintainability-index-1.0.0.jar
  
  script:
    - |
      java -jar repo-maintainability-index-1.0.0.jar \
        analyze $CI_PROJECT_PATH \
        --token $GITHUB_TOKEN \
        --format json > report.json
    - |
      SCORE=$(jq -r '.overallScore' report.json)
      echo "Quality Score: $SCORE"
      if [ $(echo "$SCORE < 70" | bc) -eq 1 ]; then
        echo "Quality gate failed"
        exit 1
      fi
  
  artifacts:
    reports:
      json: report.json
    expire_in: 30 days
```

### Scenario 3: Jenkins Pipeline

```groovy
// Jenkinsfile
pipeline {
    agent any
    
    environment {
        GITHUB_TOKEN = credentials('github-token-id')
        RMI_VERSION = '1.0.0'
    }
    
    stages {
        stage('Setup') {
            steps {
                sh """
                    wget https://github.com/YOUR-ORG/rmi/releases/download/v${RMI_VERSION}/repo-maintainability-index-${RMI_VERSION}.jar
                """
            }
        }
        
        stage('Analyze') {
            steps {
                sh """
                    java -jar repo-maintainability-index-${RMI_VERSION}.jar \
                        analyze ${env.GIT_URL} \
                        --token ${GITHUB_TOKEN} \
                        --format json > report.json
                """
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    def report = readJSON file: 'report.json'
                    def score = report.overallScore
                    
                    echo "Quality Score: ${score}"
                    
                    if (score < 70) {
                        error("Quality gate failed: score ${score} < 70")
                    }
                }
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: 'report.json', fingerprint: true
        }
    }
}
```

### Scenario 4: Cron Job (Scheduled Analysis)

```bash
#!/bin/bash
# /etc/cron.daily/rmi-analysis

# Configuration
REPOS=(
    "facebook/react"
    "microsoft/vscode"
    "vercel/next.js"
)
OUTPUT_DIR="/var/reports/rmi"
GITHUB_TOKEN="ghp_xxxxxxxxxxxx"

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Analyze repositories
for repo in "${REPOS[@]}"; do
    echo "Analyzing $repo..."
    
    # Generate filename
    filename="${repo//\//-}-$(date +%Y%m%d).json"
    
    # Run analysis
    rmi analyze "$repo" \
        --token "$GITHUB_TOKEN" \
        --format json > "$OUTPUT_DIR/$filename"
    
    # Check exit code
    if [ $? -eq 0 ]; then
        echo "✅ $repo analysis completed"
    else
        echo "❌ $repo analysis failed"
    fi
done

# Cleanup old reports (keep last 30 days)
find "$OUTPUT_DIR" -type f -mtime +30 -delete
```

**Add to crontab**:
```bash
# Run daily at 2 AM
0 2 * * * /usr/local/bin/rmi-analysis.sh >> /var/log/rmi/cron.log 2>&1
```

---

## Troubleshooting

### Common Issues

#### Issue 1: Java Not Found

**Symptoms**:
```
bash: java: command not found
```

**Solution**:
```bash
# Install Java (Ubuntu/Debian)
$ sudo apt update
$ sudo apt install openjdk-17-jre

# Install Java (RHEL/CentOS)
$ sudo yum install java-17-openjdk

# Install Java (macOS with Homebrew)
$ brew install openjdk@17

# Verify installation
$ java -version
```

#### Issue 2: Permission Denied

**Symptoms**:
```
bash: /usr/local/bin/rmi: Permission denied
```

**Solution**:
```bash
$ sudo chmod +x /usr/local/bin/rmi
```

#### Issue 3: Unicode Display Issues (Windows/GitBash)

**Symptoms**:
```
ΓòÉΓòÉΓòÉ instead of ═══
```

**Solution**:
```bash
# Use provided launcher script
$ run-with-encoding.bat analyze facebook/react

# Or add -Dfile.encoding=UTF-8
$ java -Dfile.encoding=UTF-8 -jar rmi.jar analyze facebook/react
```

#### Issue 4: GitHub API Rate Limit

**Symptoms**:
```
Error: GitHub API rate limit exceeded
```

**Solution**:
```bash
# Use authentication token
$ export GITHUB_TOKEN="ghp_xxxxxxxxxxxx"
$ rmi analyze facebook/react
```

#### Issue 5: OutOfMemoryError

**Symptoms**:
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution**:
```bash
# Increase heap size
$ java -Xmx512m -jar /opt/rmi/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

### Debug Mode

Enable verbose logging for troubleshooting:

```bash
# Set log level to DEBUG
$ export LOG_LEVEL=DEBUG
$ rmi analyze facebook/react

# Or with Java system property
$ java -Dlogback.level=DEBUG -jar rmi.jar analyze facebook/react
```

---

## Uninstallation

### Remove RMI

```bash
# Remove wrapper script
$ sudo rm /usr/local/bin/rmi

# Remove symlink
$ sudo rm /usr/local/bin/rmi.jar

# Remove installation directory
$ sudo rm -rf /opt/rmi

# Remove logs (if any)
$ sudo rm -rf /var/log/rmi

# Remove environment variables
# Edit ~/.bashrc or ~/.zshrc and remove:
# export GITHUB_TOKEN=...
# export OPENROUTER_API_KEY=...
```

### Clean Configuration

```bash
# Remove .env files
$ rm ~/.rmi/.env
$ rm .env

# Clear environment variables
$ unset GITHUB_TOKEN
$ unset OPENROUTER_API_KEY
$ unset OPENROUTER_MODEL
```

---

## Appendix

### A. Deployment Checklist

- [ ] Java 17+ installed and verified
- [ ] RMI JAR downloaded and verified (checksum)
- [ ] Wrapper script created and executable
- [ ] GitHub token generated and configured
- [ ] Network connectivity verified
- [ ] Test analysis successful
- [ ] CI/CD integration configured (if applicable)
- [ ] Logging configured
- [ ] Monitoring setup (if applicable)
- [ ] Documentation reviewed by team

### B. Support Resources

- **GitHub Issues**: https://github.com/YOUR-ORG/repo-maintainability-index/issues
- **Documentation**: `/docs/README.md`
- **Security**: `SECURITY_BEST_PRACTICES.md`
- **Operations**: `OPERATIONS_RUNBOOK.md` (to be created)

### C. Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-01-17 | Initial release |

---

**Document Version**: 1.0  
**Last Updated**: January 17, 2025  
**Next Review**: Q1 2025
