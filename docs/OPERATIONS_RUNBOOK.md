# Operations Runbook: Repository Maintainability Index

**Version**: 1.0.0  
**Last Updated**: November 17, 2024  
**On-Call Contact**: SRE Team  
**Escalation**: Development Team Lead

---

## Table of Contents

1. [Service Overview](#service-overview)
2. [Health Monitoring](#health-monitoring)
3. [Common Operational Procedures](#common-operational-procedures)
4. [Troubleshooting Guide](#troubleshooting-guide)
5. [Performance Tuning](#performance-tuning)
6. [Incident Response](#incident-response)
7. [Maintenance Procedures](#maintenance-procedures)
8. [Emergency Contacts](#emergency-contacts)

---

## Service Overview

### What is RMI?

**Repository Maintainability Index (RMI)** is a command-line tool for analyzing GitHub repository quality and maintainability metrics.

### Service Characteristics

| Characteristic | Value | Notes |
|----------------|-------|-------|
| **Type** | CLI Application | Not a web service |
| **Execution Mode** | On-demand | User-triggered or scheduled |
| **Runtime** | Java 17 JVM | Single-threaded |
| **Memory** | 50-200 MB heap | Repository size dependent |
| **Typical Duration** | 3-15 seconds | Depends on repo size |
| **Dependencies** | GitHub API, OpenRouter API (optional) | External HTTP services |

### System Dependencies

```
┌──────────────────────┐
│   RMI Application    │
└──────────┬───────────┘
           │
    ┌──────┴─────────┬──────────────┐
    │                │              │
    ▼                ▼              ▼
┌────────┐    ┌──────────┐   ┌──────────┐
│ Java 17│    │ GitHub   │   │OpenRouter│
│  JRE   │    │   API    │   │   API    │
└────────┘    └──────────┘   └──────────┘
              (Critical)     (Optional)
```

---

## Health Monitoring

### Service Health Indicators

#### 1. **Application Health**

##### Check Command:
```bash
rmi --version
```

**Expected Output**:
```
Repository Maintainability Index CLI 1.0.0
```

**Health Status**:
- ✅ **HEALTHY**: Version displays correctly
- ❌ **UNHEALTHY**: Command fails or wrong version

##### Monitoring Script:
```bash
#!/bin/bash
# /usr/local/bin/rmi-health-check

set -e

# Check RMI availability
if ! rmi --version > /dev/null 2>&1; then
    echo "CRITICAL: RMI not available"
    exit 2
fi

# Check test analysis
if ! timeout 30s rmi analyze picocli/picocli --format json > /tmp/rmi-health.json 2>&1; then
    echo "CRITICAL: RMI analysis failed"
    exit 2
fi

# Check score exists in output
if ! jq -e '.overallScore' /tmp/rmi-health.json > /dev/null 2>&1; then
    echo "CRITICAL: Invalid output format"
    exit 2
fi

echo "OK: RMI is healthy"
exit 0
```

#### 2. **GitHub API Health**

##### Check Command:
```bash
curl -sf https://api.github.com/zen
```

**Expected Output**: Random zen quote

**Health Status**:
- ✅ **HEALTHY**: Returns 200 OK with content
- ⚠️ **DEGRADED**: Slow response (>2s)
- ❌ **UNHEALTHY**: Connection failure or 5xx errors

##### Rate Limit Check:
```bash
#!/bin/bash
# Check GitHub API rate limits

TOKEN="${GITHUB_TOKEN}"

if [ -z "$TOKEN" ]; then
    echo "⚠️  No token configured - using unauthenticated rate limits"
    RESPONSE=$(curl -sf https://api.github.com/rate_limit)
else
    RESPONSE=$(curl -sf -H "Authorization: Bearer $TOKEN" https://api.github.com/rate_limit)
fi

REMAINING=$(echo "$RESPONSE" | jq -r '.rate.remaining')
LIMIT=$(echo "$RESPONSE" | jq -r '.rate.limit')
RESET=$(echo "$RESPONSE" | jq -r '.rate.reset')
RESET_TIME=$(date -d "@$RESET" 2>/dev/null || date -r "$RESET")

echo "GitHub API Rate Limits:"
echo "  Remaining: $REMAINING / $LIMIT"
echo "  Resets at: $RESET_TIME"

if [ "$REMAINING" -lt 10 ]; then
    echo "❌ CRITICAL: Rate limit nearly exhausted"
    exit 2
elif [ "$REMAINING" -lt 100 ]; then
    echo "⚠️  WARNING: Rate limit running low"
    exit 1
else
    echo "✅ OK: Sufficient rate limit"
    exit 0
fi
```

#### 3. **LLM API Health** (Optional)

##### Check Command:
```bash
curl -sf https://openrouter.ai/api/v1/models | jq -r '.data[0].id'
```

**Expected Output**: Model ID (e.g., `openai/gpt-3.5-turbo`)

**Health Status**:
- ✅ **HEALTHY**: Returns model list
- ❌ **UNHEALTHY**: Connection failure (graceful fallback in RMI)

### Key Metrics to Monitor

| Metric | Description | Warning Threshold | Critical Threshold |
|--------|-------------|-------------------|-------------------|
| **Analysis Duration** | Time to complete analysis | > 30s | > 60s |
| **Success Rate** | % of successful analyses | < 95% | < 90% |
| **GitHub Rate Limit** | Remaining API calls | < 100 | < 10 |
| **Memory Usage** | JVM heap utilization | > 75% | > 90% |
| **Error Rate** | API/network failures | > 5% | > 10% |

### Recommended Monitoring Setup

```bash
# Prometheus exporter (future enhancement)
# NOT YET IMPLEMENTED - placeholder for future

# Example metrics endpoint (if implemented):
# http://localhost:9090/metrics

# Metrics to expose:
# - rmi_analysis_duration_seconds_bucket
# - rmi_analysis_total{status="success|failure"}
# - rmi_github_rate_limit_remaining
# - rmi_jvm_memory_used_bytes
# - rmi_errors_total{type="network|api|internal"}
```

---

## Common Operational Procedures

### Procedure 1: Running Manual Analysis

**When**: On-demand quality assessment

**Steps**:
```bash
# 1. Set environment
export GITHUB_TOKEN="ghp_xxxxxxxxxxxx"

# 2. Run analysis
rmi analyze owner/repo --format json > report.json

# 3. Verify output
jq '.overallScore' report.json

# 4. Check exit code
if [ $? -eq 0 ]; then
    echo "✅ Analysis successful"
else
    echo "❌ Analysis failed"
fi
```

**Expected Duration**: 3-15 seconds

### Procedure 2: Batch Repository Analysis

**When**: Regular quality audits

**Steps**:
```bash
#!/bin/bash
# batch-analysis.sh

REPOS_FILE="repositories.txt"
OUTPUT_DIR="reports/$(date +%Y%m%d)"
mkdir -p "$OUTPUT_DIR"

# Read repositories from file (one per line)
while IFS= read -r repo; do
    echo "Analyzing $repo..."
    
    filename="${repo//\//-}.json"
    
    if rmi analyze "$repo" --format json > "$OUTPUT_DIR/$filename" 2>&1; then
        echo "✅ $repo completed"
    else
        echo "❌ $repo failed"
    fi
    
    # Rate limiting protection
    sleep 2
done < "$REPOS_FILE"

echo "Batch analysis complete: $OUTPUT_DIR"
```

**Expected Duration**: ~5-10 seconds per repository

### Procedure 3: Updating RMI Version

**When**: New version available

**Steps**:
```bash
# 1. Backup current version
sudo cp /opt/rmi/repo-maintainability-index-1.0.0.jar \
     /opt/rmi/repo-maintainability-index-1.0.0.jar.bak

# 2. Download new version
wget https://github.com/YOUR-ORG/rmi/releases/download/v1.1.0/repo-maintainability-index-1.1.0.jar

# 3. Verify checksum
sha256sum -c repo-maintainability-index-1.1.0.jar.sha256

# 4. Install new version
sudo mv repo-maintainability-index-1.1.0.jar /opt/rmi/

# 5. Update symlink
sudo ln -sf /opt/rmi/repo-maintainability-index-1.1.0.jar /usr/local/bin/rmi.jar

# 6. Verify
rmi --version

# 7. Test
rmi analyze picocli/picocli

# 8. If successful, remove backup
# sudo rm /opt/rmi/repo-maintainability-index-1.0.0.jar.bak
```

**Rollback**:
```bash
# If new version fails, restore backup
sudo ln -sf /opt/rmi/repo-maintainability-index-1.0.0.jar.bak /usr/local/bin/rmi.jar
```

### Procedure 4: Rotating API Tokens

**When**: Monthly or after security incident

**Steps**:
```bash
# 1. Generate new GitHub token
# Visit: https://github.com/settings/tokens/new

# 2. Test new token
GITHUB_TOKEN="ghp_NEW_TOKEN" rmi analyze picocli/picocli

# 3. Update environment
# Edit ~/.bashrc or /etc/environment
export GITHUB_TOKEN="ghp_NEW_TOKEN"

# 4. Update CI/CD secrets
# GitHub Actions: Settings > Secrets > Update GITHUB_TOKEN

# 5. Revoke old token
# Visit: https://github.com/settings/tokens

# 6. Verify all systems updated
```

---

## Troubleshooting Guide

### Problem: Analysis Failing with "Repository Not Found"

**Symptoms**:
```
Error: Repository not found: owner/repo
GitHub API returned 404 Not Found
```

**Possible Causes**:
1. Repository name typo
2. Repository is private and token lacks access
3. Repository deleted/renamed

**Diagnostic Steps**:
```bash
# 1. Verify repository exists
curl -I https://github.com/owner/repo

# 2. Check API access
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
     https://api.github.com/repos/owner/repo

# 3. Verify token scopes
# Visit: https://github.com/settings/tokens
```

**Resolution**:
- Correct repository name
- Use token with appropriate scopes
- For private repos: add `repo` scope

**Priority**: P3 (Low) - User error

---

### Problem: Rate Limit Exceeded

**Symptoms**:
```
Error: GitHub API rate limit exceeded
Limit: 60/hour (unauthenticated)
Reset at: 2024-11-17 15:30:00 UTC
```

**Possible Causes**:
1. No authentication token configured
2. Too many analyses in short period
3. Shared token exhausted by other services

**Diagnostic Steps**:
```bash
# Check current rate limit
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
     https://api.github.com/rate_limit | jq '.rate'

# Output:
# {
#   "limit": 5000,
#   "remaining": 4950,
#   "reset": 1700233800
# }
```

**Resolution**:

**Short-term**:
```bash
# Wait until reset time
RESET=$(curl -H "Authorization: Bearer $GITHUB_TOKEN" \
     https://api.github.com/rate_limit | jq -r '.rate.reset')
echo "Rate limit resets at: $(date -d "@$RESET")"
```

**Long-term**:
```bash
# 1. Configure authentication token
export GITHUB_TOKEN="ghp_xxxxxxxxxxxx"

# 2. Use multiple tokens (round-robin)
# 3. Implement caching
# 4. Add rate limit backoff
```

**Priority**: P2 (Medium)

---

### Problem: Analysis Extremely Slow

**Symptoms**:
- Analysis takes > 60 seconds
- Appears hung/frozen

**Possible Causes**:
1. Very large repository (>100k commits)
2. Network latency to GitHub API
3. API rate limiting causing retries
4. Insufficient memory

**Diagnostic Steps**:
```bash
# 1. Check repository size
rmi analyze owner/repo --format json | \
  jq '.metrics.Activity.details'

# 2. Monitor network latency
ping api.github.com

# 3. Check memory usage
jps -v | grep rmi

# 4. Enable debug logging
LOG_LEVEL=DEBUG rmi analyze owner/repo
```

**Resolution**:

```bash
# 1. Increase timeout (if needed in future)
java -Xmx512m -jar rmi.jar analyze owner/repo

# 2. Check network connectivity
traceroute api.github.com

# 3. Use authenticated token for better rate limits
export GITHUB_TOKEN="ghp_xxxxxxxxxxxx"
```

**Priority**: P2 (Medium)

---

### Problem: OutOfMemoryError

**Symptoms**:
```
java.lang.OutOfMemoryError: Java heap space
```

**Possible Causes**:
1. Very large repository data
2. Insufficient heap size
3. Memory leak (rare)

**Diagnostic Steps**:
```bash
# Check default heap size
java -XX:+PrintFlagsFinal -version | grep HeapSize

# Monitor memory during analysis
java -Xlog:gc* -jar rmi.jar analyze owner/repo
```

**Resolution**:

```bash
# Increase heap size
java -Xmx1g -jar /opt/rmi/repo-maintainability-index-1.0.0.jar \
  analyze owner/repo

# For very large repos
java -Xmx2g -jar rmi.jar analyze owner/repo
```

**Priority**: P3 (Low) - Edge case

---

### Problem: Unicode Display Issues (GitBash/Windows)

**Symptoms**:
```
ΓòÉΓòÉ instead of ═══
```

**Resolution**:
```bash
# Use launcher script
run-with-encoding.bat analyze owner/repo

# Or manual fix
java -Dfile.encoding=UTF-8 -jar rmi.jar analyze owner/repo

# GitBash configuration
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
```

**Priority**: P4 (Low) - Display only

---

## Performance Tuning

### JVM Tuning

#### Recommended JVM Options:

```bash
# Balanced configuration (default)
java -Xmx256m -Xms128m \
     -Dfile.encoding=UTF-8 \
     -jar rmi.jar analyze owner/repo

# High-performance configuration (large repos)
java -Xmx512m -Xms256m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -Dfile.encoding=UTF-8 \
     -jar rmi.jar analyze owner/repo

# Low-memory configuration (resource-constrained)
java -Xmx128m -Xms64m \
     -XX:+UseSerialGC \
     -Dfile.encoding=UTF-8 \
     -jar rmi.jar analyze owner/repo
```

### Network Optimization

#### Connection Pooling (Future Enhancement):

```java
// NOT YET IMPLEMENTED - Future optimization
OkHttpClient client = new OkHttpClient.Builder()
    .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
    .retryOnConnectionFailure(true)
    .build();
```

#### DNS Caching:

```bash
# Enable JVM DNS caching
java -Dsun.net.inetaddr.ttl=60 \
     -Dsun.net.inetaddr.negative.ttl=10 \
     -jar rmi.jar analyze owner/repo
```

### Parallelization (Future Enhancement)

**Current**: Sequential metric calculation  
**Future**: Parallel execution

```bash
# NOT YET IMPLEMENTED
# Future: Parallel metric calculation
java -Drmi.parallel=true \
     -Drmi.threads=6 \
     -jar rmi.jar analyze owner/repo
```

---

## Incident Response

### Incident Severity Levels

| Severity | Description | Response Time | Example |
|----------|-------------|---------------|---------|
| **P0 (Critical)** | Complete service outage | 15 minutes | RMI completely unusable |
| **P1 (High)** | Major functionality degraded | 1 hour | All analyses failing |
| **P2 (Medium)** | Minor functionality issues | 4 hours | Single metric failing |
| **P3 (Low)** | Cosmetic or edge cases | Next business day | Display formatting issue |

### Incident Response Playbook

#### P0: Complete Service Outage

**Detection**:
```bash
# Health check fails
rmi --version
# Error: command not found
```

**Response Steps**:

1. **Acknowledge Incident** (T+0 min)
   ```bash
   # Verify outage
   which rmi
   java --version
   ```

2. **Initial Diagnosis** (T+5 min)
   ```bash
   # Check installation
   ls -la /opt/rmi/
   ls -la /usr/local/bin/rmi
   
   # Check Java
   java -version
   ```

3. **Restore Service** (T+15 min)
   ```bash
   # Reinstall from backup or download
   sudo ln -sf /opt/rmi/repo-maintainability-index-1.0.0.jar /usr/local/bin/rmi.jar
   
   # Verify
   rmi --version
   ```

4. **Post-Incident** (T+1 hour)
   - Document root cause
   - Update runbook
   - Implement preventive measures

#### P1: All Analyses Failing

**Detection**:
```bash
# All analyses return errors
rmi analyze picocli/picocli
# Error: GitHub API request failed: 503
```

**Response Steps**:

1. **Check GitHub API Status**
   ```bash
   curl https://www.githubstatus.com/api/v2/status.json | jq '.status.description'
   ```

2. **Check Rate Limits**
   ```bash
   curl -H "Authorization: Bearer $GITHUB_TOKEN" \
        https://api.github.com/rate_limit
   ```

3. **Check Token Validity**
   ```bash
   curl -H "Authorization: Bearer $GITHUB_TOKEN" \
        https://api.github.com/user
   ```

4. **Escalate if Needed**
   - If GitHub API down: Wait for GitHub resolution
   - If token invalid: Rotate token
   - If internal issue: Escalate to dev team

---

## Maintenance Procedures

### Daily Maintenance

#### Health Check (Automated)

```bash
#!/bin/bash
# /etc/cron.daily/rmi-health-check

# Run health check
/usr/local/bin/rmi-health-check

# Log results
echo "$(date): Health check completed" >> /var/log/rmi/health.log
```

### Weekly Maintenance

#### Log Rotation

```bash
# /etc/logrotate.d/rmi
/var/log/rmi/*.log {
    weekly
    rotate 4
    compress
    missingok
    notifempty
}
```

#### Dependency Check

```bash
# Check for RMI updates
latest_version=$(curl -s https://api.github.com/repos/YOUR-ORG/rmi/releases/latest | jq -r '.tag_name')
current_version=$(rmi --version | awk '{print $NF}')

if [ "$latest_version" != "v$current_version" ]; then
    echo "⚠️  New version available: $latest_version (current: $current_version)"
fi
```

### Monthly Maintenance

#### Token Rotation

```bash
# Rotate GitHub tokens monthly
# See "Procedure 4: Rotating API Tokens"
```

#### Performance Review

```bash
# Analyze performance trends
grep "Completed maintainability analysis" /var/log/rmi/*.log | \
  awk '{print $NF}' | \
  sort -n | \
  awk '{sum+=$1; count++} END {print "Average:", sum/count, "seconds"}'
```

---

## Emergency Contacts

### Primary Contacts

| Role | Contact | Availability |
|------|---------|--------------|
| **SRE On-Call** | sre-oncall@company.com | 24/7 |
| **Dev Team Lead** | dev-lead@company.com | Business hours |
| **Security Team** | security@company.com | 24/7 (security only) |

### Escalation Path

```
User Issue
    │
    ▼
Operations Team (15 min)
    │
    ▼
SRE Team (1 hour)
    │
    ▼
Development Team (4 hours)
    │
    ▼
Engineering Manager (8 hours)
```

### External Contacts

| Service | Status Page | Support |
|---------|-------------|---------|
| **GitHub** | https://www.githubstatus.com/ | https://support.github.com/ |
| **OpenRouter** | https://openrouter.ai/status | support@openrouter.ai |

---

## Appendix

### A. Useful Commands Reference

```bash
# Check RMI version
rmi --version

# Test analysis
rmi analyze picocli/picocli

# Check rate limits
curl -H "Authorization: Bearer $GITHUB_TOKEN" https://api.github.com/rate_limit

# View logs (if configured)
tail -f /var/log/rmi/application.log

# Monitor JVM memory
jps -v | grep rmi
jmap -heap <pid>

# Network diagnostics
ping api.github.com
traceroute api.github.com
curl -I https://api.github.com/zen
```

### B. Configuration Files

```
/opt/rmi/                              # Installation directory
├── repo-maintainability-index-1.0.0.jar
└── logback.xml                        # Custom logging config (optional)

/usr/local/bin/
├── rmi                                # Wrapper script
└── rmi.jar -> /opt/rmi/...            # Symlink

/etc/cron.daily/
└── rmi-health-check                   # Automated health check

/var/log/rmi/
├── application.log                    # Application logs (if configured)
├── health.log                         # Health check logs
└── cron.log                           # Cron job logs
```

---

**Document Version**: 1.0  
**Last Updated**: November 17, 2024  
**Next Review**: Q1 2025  
**Document Owner**: SRE Team
