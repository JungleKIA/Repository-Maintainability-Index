# Operations Runbook

**Service:** Repository Maintainability Index (RMI)  
**Version:** 1.0.0  
**Last Updated:** 2024  
**On-Call Team:** DevOps/Platform Engineering

---

## Table of Contents

1. [Service Overview](#service-overview)
2. [Architecture Quick Reference](#architecture-quick-reference)
3. [Common Operations](#common-operations)
4. [Troubleshooting Guide](#troubleshooting-guide)
5. [Incident Response](#incident-response)
6. [Monitoring & Alerts](#monitoring--alerts)
7. [Performance Tuning](#performance-tuning)
8. [Maintenance Procedures](#maintenance-procedures)
9. [Emergency Contacts](#emergency-contacts)

---

## Service Overview

### What is RMI?
Repository Maintainability Index is a CLI tool that analyzes GitHub repositories for quality and maintainability metrics.

### Service Responsibilities
- Fetch repository data from GitHub API
- Calculate 6 maintainability metrics
- Optional AI-powered analysis via OpenRouter
- Generate reports in text or JSON format

### Service Dependencies
| Dependency | Type | Critical? | Fallback |
|------------|------|-----------|----------|
| GitHub API | External | Yes | None - analysis fails |
| OpenRouter API | External | No | Skip LLM analysis |
| Internet Connection | Infrastructure | Yes | None |

### SLOs (Service Level Objectives)
| Metric | Target | Measurement |
|--------|--------|-------------|
| Availability | 99.5% | Successful analysis runs |
| Latency (standard) | p95 < 5s | Analysis completion time |
| Latency (LLM) | p95 < 15s | Analysis with AI |
| Error Rate | < 1% | Failed analyses |
| GitHub Rate Limit | < 80% | API quota usage |

---

## Architecture Quick Reference

### Component Diagram
```
┌──────────────┐
│   User/CLI   │
└──────┬───────┘
       │
       ▼
┌──────────────────────────────┐
│   RMI Application (Java)     │
│                              │
│  ┌────────────────────────┐ │
│  │  MaintainabilityService│ │
│  └───────┬────────────────┘ │
│          │                   │
│  ┌───────▼───────┐          │
│  │ MetricCalc... │          │
│  └───────┬───────┘          │
└──────────┼───────────────────┘
           │
     ┌─────┴─────┐
     ▼           ▼
┌─────────┐ ┌─────────┐
│ GitHub  │ │ OpenRouter│
│   API   │ │   API    │
└─────────┘ └─────────┘
```

### Key Components
- **Main.java** - Entry point, CLI initialization
- **AnalyzeCommand** - Command handler
- **MaintainabilityService** - Core analysis logic
- **MetricCalculators** - Individual metric implementations
- **GitHubClient** - GitHub API communication
- **LLMClient** - AI analysis integration

### File Locations
```
/home/engine/project/
├── target/repo-maintainability-index-1.0.0.jar
├── .env (environment configuration)
├── logs/ (application logs - if configured)
└── docs/ (documentation)
```

---

## Common Operations

### Start/Run Analysis

#### Standard Analysis
```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

#### With GitHub Token
```bash
export GITHUB_TOKEN=your_token
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

#### With AI Analysis
```bash
export GITHUB_TOKEN=your_github_token
export OPENROUTER_API_KEY=your_llm_key
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm
```

#### Docker Execution
```bash
docker run --rm \
  -e GITHUB_TOKEN=your_token \
  -e OPENROUTER_API_KEY=your_llm_key \
  rmi-app analyze owner/repo --llm
```

### Check Service Health

#### Verify JAR Integrity
```bash
java -jar target/repo-maintainability-index-1.0.0.jar --version
# Expected: "rmi 1.0.0"
```

#### Test GitHub API Connectivity
```bash
curl -H "Authorization: Bearer $GITHUB_TOKEN" https://api.github.com/rate_limit
```

#### Test OpenRouter API Connectivity
```bash
curl -X POST https://openrouter.ai/api/v1/chat/completions \
  -H "Authorization: Bearer $OPENROUTER_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"openai/gpt-3.5-turbo","messages":[{"role":"user","content":"test"}]}'
```

### View Logs

#### Console Output
Standard output shows analysis progress and results.

#### Enable Debug Logging
```bash
export LOG_LEVEL=DEBUG
java -Dlogback.configurationFile=logback-debug.xml -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

#### Log File Configuration
Edit `src/main/resources/logback.xml` to add file appender:
```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>logs/rmi.log</file>
    <encoder>
        <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

### Configuration Changes

#### Update GitHub Token
```bash
# Option 1: Environment variable
export GITHUB_TOKEN=new_token

# Option 2: .env file
echo "GITHUB_TOKEN=new_token" > .env
```

#### Update LLM Model
```bash
export OPENROUTER_MODEL=openai/gpt-4
# Or use --model flag
java -jar rmi.jar analyze owner/repo --llm --model openai/gpt-4
```

#### Adjust Timeouts
No direct configuration available. Requires code change in GitHubClient/LLMClient.

---

## Troubleshooting Guide

### Issue: "GitHub API rate limit exceeded"

**Symptoms:**
- Error message: "API rate limit exceeded"
- Analysis fails immediately
- HTTP 403 response from GitHub

**Diagnosis:**
```bash
# Check rate limit status
curl -H "Authorization: Bearer $GITHUB_TOKEN" https://api.github.com/rate_limit

# Look for:
# "remaining": 0
# "reset": <timestamp>
```

**Resolution:**
1. **Immediate**: Wait until rate limit resets (check "reset" timestamp)
2. **Short-term**: Use authenticated requests (set GITHUB_TOKEN)
3. **Long-term**: 
   - Implement caching for GitHub responses
   - Use multiple tokens with round-robin rotation
   - Contact GitHub for higher rate limits

**Prevention:**
- Always use authenticated requests (5000 req/hr vs 60 req/hr)
- Monitor rate limit consumption
- Implement exponential backoff

---

### Issue: "Connection timeout to GitHub API"

**Symptoms:**
- Error: "SocketTimeoutException" or "ConnectException"
- Analysis hangs for 30+ seconds
- No response from GitHub

**Diagnosis:**
```bash
# Test network connectivity
ping api.github.com

# Test HTTPS connectivity
curl -v https://api.github.com

# Check DNS resolution
nslookup api.github.com
```

**Resolution:**
1. Verify internet connectivity
2. Check firewall rules (allow HTTPS to api.github.com)
3. Check proxy settings (if behind corporate proxy)
4. Verify GitHub status: https://www.githubstatus.com/
5. Retry analysis (transient network issues)

**Prevention:**
- Implement retry logic with exponential backoff
- Monitor GitHub API status
- Configure connection timeouts appropriately

---

### Issue: "LLM analysis fails but analysis continues"

**Symptoms:**
- Warning: "LLM analysis failed, continuing with deterministic metrics only"
- Analysis completes without AI insights
- OpenRouter API errors in logs

**Diagnosis:**
```bash
# Verify API key
curl -X POST https://openrouter.ai/api/v1/chat/completions \
  -H "Authorization: Bearer $OPENROUTER_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"openai/gpt-3.5-turbo","messages":[{"role":"user","content":"test"}]}'

# Check for errors:
# - 401 Unauthorized: Invalid API key
# - 429 Too Many Requests: Rate limit exceeded
# - 500 Internal Server Error: OpenRouter issue
```

**Resolution:**
1. **Invalid API Key**: Update OPENROUTER_API_KEY with valid key
2. **Rate Limit**: Wait or upgrade OpenRouter plan
3. **OpenRouter Outage**: Check https://status.openrouter.ai/
4. **Network Issue**: Verify connectivity to openrouter.ai
5. **Acceptable**: Analysis continues without AI (by design)

**Prevention:**
- Monitor OpenRouter API status
- Set up rate limit alerts
- Consider backup LLM providers

---

### Issue: "Unicode characters display as garbled text"

**Symptoms:**
- See `ΓòÉ` instead of `═`
- See `ΓöÇ` instead of `─`
- Box-drawing characters corrupted

**Diagnosis:**
```bash
# Check system locale
echo $LANG
# Should be: en_US.UTF-8

# Check Java encoding
java -XshowSettings:properties -version 2>&1 | grep file.encoding
# Should include: file.encoding = UTF-8
```

**Resolution:**

**Option 1: Use provided scripts (RECOMMENDED)**
```bash
# Windows
run-with-encoding.bat analyze owner/repo

# Linux/macOS/GitBash
./run-with-encoding.sh analyze owner/repo
```

**Option 2: Add encoding flag manually**
```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

**Option 3: Configure Git Bash (Windows)**
```bash
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
```

**Option 4: Change terminal font**
- Use Consolas, Cascadia Code, or JetBrains Mono
- Ensure font supports Unicode box-drawing characters

**Prevention:**
- Always use `-Dfile.encoding=UTF-8` flag
- Use provided launcher scripts
- Configure Git Bash with UTF-8 locale

---

### Issue: "Analysis extremely slow (>60 seconds)"

**Symptoms:**
- Analysis takes 60+ seconds
- LLM analysis significantly slower than expected
- High CPU/memory usage

**Diagnosis:**
```bash
# Monitor resource usage
top -p $(pgrep -f repo-maintainability-index)

# Check network latency
ping api.github.com
ping openrouter.ai

# Enable debug logging
export LOG_LEVEL=DEBUG
```

**Possible Causes:**
1. **Large Repository**: Repos with 1000+ commits slow down commit analysis
2. **Slow LLM Response**: OpenRouter API latency varies by model
3. **Network Latency**: High RTT to GitHub/OpenRouter
4. **Rate Limiting**: GitHub throttling requests

**Resolution:**
1. **Large repos**: Expected behavior (commit analysis is sequential)
2. **Slow LLM**: 
   - Try faster model: `--model openai/gpt-3.5-turbo`
   - Skip LLM: Remove `--llm` flag
3. **Network**: Check connection quality, try different network
4. **Rate limiting**: Check rate limit status, wait if exceeded

**Prevention:**
- Cache GitHub API responses (future enhancement)
- Parallel metric calculations (future enhancement)
- Async LLM calls (future enhancement)

---

### Issue: "Out of memory error"

**Symptoms:**
- Error: "java.lang.OutOfMemoryError: Java heap space"
- Analysis crashes
- High memory usage (>200MB)

**Diagnosis:**
```bash
# Check Java heap size
java -XX:+PrintFlagsFinal -version | grep HeapSize

# Monitor memory during analysis
watch -n 1 'jps -v | grep repo-maintainability-index'
```

**Resolution:**
1. **Increase heap size**:
```bash
java -Xmx512m -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

2. **Check repository size**: Extremely large repos (10k+ commits) may require more memory

3. **Restart JVM**: Memory leaks unlikely but restart clears heap

**Prevention:**
- Set appropriate heap size for large repositories
- Monitor memory usage
- Implement streaming for large datasets (future enhancement)

---

### Issue: "JSON output is malformed"

**Symptoms:**
- JSON parsing errors
- Incomplete JSON output
- Missing fields

**Diagnosis:**
```bash
# Validate JSON output
java -jar rmi.jar analyze owner/repo --format json | jq .

# Check for errors in stderr
java -jar rmi.jar analyze owner/repo --format json 2>errors.log
```

**Resolution:**
1. **Check stderr**: Error messages may corrupt JSON output
2. **Redirect stderr**: 
```bash
java -jar rmi.jar analyze owner/repo --format json 2>/dev/null
```
3. **Verify repository exists**: Non-existent repos may cause partial JSON
4. **File bug report**: If JSON consistently malformed

**Prevention:**
- Separate stdout (JSON) and stderr (errors)
- Validate JSON schema
- Add JSON output tests (test suite enhancement)

---

### Issue: "Docker container fails to start"

**Symptoms:**
- Container exits immediately
- Error: "exec format error"
- Health check failures

**Diagnosis:**
```bash
# Check container logs
docker logs <container_id>

# Verify image architecture
docker inspect rmi-app | grep Architecture

# Test container interactively
docker run -it --entrypoint /bin/sh rmi-app
```

**Resolution:**
1. **Exec format error**: Pull correct architecture (AMD64 vs ARM64)
```bash
docker pull --platform linux/amd64 rmi-app
```

2. **Missing environment variables**:
```bash
docker run --rm \
  -e GITHUB_TOKEN=your_token \
  rmi-app analyze owner/repo
```

3. **Permission issues**: Verify non-root user has permissions

4. **Health check failure**: Increase health check timeout

**Prevention:**
- Use multi-platform images (AMD64, ARM64)
- Document required environment variables
- Test container before deployment

---

## Incident Response

### Severity Levels

#### Severity 1 (Critical) - Immediate Response
- **Definition**: Complete service outage, data loss, security breach
- **Response Time**: 15 minutes
- **Examples**:
  - GitHub API consistently unreachable
  - Security vulnerability actively exploited
  - Data corruption detected
- **Actions**:
  1. Page on-call engineer immediately
  2. Create incident channel (#incident-rmi-YYYYMMDD)
  3. Start incident log
  4. Notify stakeholders within 30 minutes
  5. Implement emergency fix or rollback

#### Severity 2 (High) - Urgent Response
- **Definition**: Significant degradation, user impact
- **Response Time**: 1 hour
- **Examples**:
  - High error rate (>10%)
  - Slow performance (>30s per analysis)
  - GitHub rate limits repeatedly exceeded
- **Actions**:
  1. Alert on-call engineer
  2. Investigate root cause
  3. Implement fix within 4 hours
  4. Post-mortem within 24 hours

#### Severity 3 (Medium) - Business Hours Response
- **Definition**: Minor issues, workarounds available
- **Response Time**: 4 hours
- **Examples**:
  - LLM analysis failures
  - Unicode display issues
  - Non-critical bugs
- **Actions**:
  1. Create ticket in issue tracker
  2. Investigate during business hours
  3. Plan fix for next release

#### Severity 4 (Low) - Planned Work
- **Definition**: Feature requests, minor enhancements
- **Response Time**: N/A
- **Examples**:
  - New metrics requests
  - Documentation improvements
  - Performance optimizations
- **Actions**:
  1. Add to backlog
  2. Prioritize during sprint planning

### Incident Response Workflow

```
Incident Detected
     │
     ▼
[Assess Severity] ─── Sev 4 ──▶ Add to backlog
     │
     ├─── Sev 3 ──▶ Create ticket
     │
     ├─── Sev 2 ──▶ Alert on-call
     │               │
     │               ▼
     │          Investigate
     │               │
     └─── Sev 1 ──▶ Page immediately ──▶ Create incident channel
                     │
                     ▼
                Implement Fix
                     │
                     ▼
                Verify Resolution
                     │
                     ▼
               Update Stakeholders
                     │
                     ▼
                 Post-Mortem
```

### Incident Communication Template

```
INCIDENT REPORT
===============
Incident ID: INC-YYYYMMDD-NNN
Severity: [1-4]
Status: [OPEN|INVESTIGATING|RESOLVED|CLOSED]
Start Time: YYYY-MM-DD HH:MM UTC
Resolution Time: YYYY-MM-DD HH:MM UTC

IMPACT:
- Affected users: [number or percentage]
- Affected functionality: [description]
- Data loss: [YES/NO - describe if yes]

ROOT CAUSE:
[Detailed explanation of what caused the incident]

TIMELINE:
HH:MM - Incident detected
HH:MM - On-call paged
HH:MM - Root cause identified
HH:MM - Fix deployed
HH:MM - Verified resolved

RESOLUTION:
[Description of fix applied]

ACTION ITEMS:
1. [Preventive measure 1] - Owner: [name] - Due: [date]
2. [Preventive measure 2] - Owner: [name] - Due: [date]

LESSONS LEARNED:
- [What went well]
- [What could be improved]
- [Action items for improvement]
```

### Rollback Procedures

#### JAR Rollback
```bash
# 1. Stop current process
pkill -f repo-maintainability-index

# 2. Backup current version
cp target/repo-maintainability-index-1.0.0.jar backup/

# 3. Restore previous version
cp backup/repo-maintainability-index-<previous>.jar target/repo-maintainability-index-1.0.0.jar

# 4. Verify version
java -jar target/repo-maintainability-index-1.0.0.jar --version

# 5. Test basic functionality
java -jar target/repo-maintainability-index-1.0.0.jar analyze facebook/react
```

#### Docker Rollback
```bash
# 1. List available tags
docker images rmi-app

# 2. Stop current container
docker stop rmi-container

# 3. Start previous version
docker run -d --name rmi-container rmi-app:<previous-tag>

# 4. Verify health
docker logs rmi-container
```

#### Kubernetes Rollback
```bash
# 1. View deployment history
kubectl rollout history deployment/rmi

# 2. Rollback to previous version
kubectl rollout undo deployment/rmi

# 3. Verify rollback
kubectl rollout status deployment/rmi

# 4. Check pods
kubectl get pods -l app=rmi
```

---

## Monitoring & Alerts

### Key Metrics to Monitor

#### Application Metrics
| Metric | Type | Threshold | Alert Level |
|--------|------|-----------|-------------|
| Analysis Success Rate | Percentage | <95% | Warning |
| Analysis Success Rate | Percentage | <90% | Critical |
| Analysis Latency (p95) | Duration | >10s | Warning |
| Analysis Latency (p95) | Duration | >20s | Critical |
| Error Rate | Percentage | >5% | Warning |
| Error Rate | Percentage | >10% | Critical |

#### External Dependency Metrics
| Metric | Type | Threshold | Alert Level |
|--------|------|-----------|-------------|
| GitHub API Latency | Duration | >2s | Warning |
| GitHub Rate Limit | Percentage | >80% | Warning |
| GitHub Rate Limit | Percentage | >95% | Critical |
| OpenRouter Latency | Duration | >15s | Warning |
| OpenRouter Error Rate | Percentage | >20% | Warning |

#### Infrastructure Metrics
| Metric | Type | Threshold | Alert Level |
|--------|------|-----------|-------------|
| Memory Usage | Percentage | >80% | Warning |
| Memory Usage | Percentage | >90% | Critical |
| CPU Usage | Percentage | >70% | Warning |
| Disk I/O Wait | Percentage | >20% | Warning |

### Recommended Monitoring Tools

#### Metrics Collection
- **Prometheus** - Time-series metrics database
- **Micrometer** - Metrics instrumentation library
- **Grafana** - Metrics visualization and dashboards

#### Log Aggregation
- **ELK Stack** (Elasticsearch, Logback, Kibana)
- **Splunk** - Enterprise log management
- **Datadog** - All-in-one observability

#### Distributed Tracing
- **Jaeger** - Distributed tracing platform
- **Zipkin** - Distributed tracing system
- **OpenTelemetry** - Observability framework

#### Error Tracking
- **Sentry** - Error tracking and monitoring
- **Rollbar** - Real-time error tracking
- **Bugsnag** - Application stability monitoring

### Sample Grafana Dashboard Panels

```json
{
  "dashboard": {
    "title": "RMI - Repository Maintainability Index",
    "panels": [
      {
        "title": "Analysis Success Rate (24h)",
        "targets": [
          {
            "expr": "rate(rmi_analysis_success_total[24h]) / rate(rmi_analysis_total[24h]) * 100"
          }
        ]
      },
      {
        "title": "Analysis Latency (p95)",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(rmi_analysis_duration_seconds_bucket[5m]))"
          }
        ]
      },
      {
        "title": "GitHub Rate Limit Consumption",
        "targets": [
          {
            "expr": "(5000 - github_rate_limit_remaining) / 5000 * 100"
          }
        ]
      },
      {
        "title": "Error Rate by Type",
        "targets": [
          {
            "expr": "rate(rmi_errors_total[5m]) by (error_type)"
          }
        ]
      }
    ]
  }
}
```

### Alert Rules (Prometheus AlertManager)

```yaml
groups:
  - name: rmi_alerts
    rules:
      - alert: HighErrorRate
        expr: rate(rmi_errors_total[5m]) / rate(rmi_analysis_total[5m]) > 0.10
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value | humanizePercentage }}"
      
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(rmi_analysis_duration_seconds_bucket[5m])) > 20
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High analysis latency"
          description: "p95 latency is {{ $value }}s"
      
      - alert: GitHubRateLimitHigh
        expr: github_rate_limit_remaining < 1000
        labels:
          severity: warning
        annotations:
          summary: "GitHub rate limit consumption high"
          description: "Only {{ $value }} requests remaining"
```

---

## Performance Tuning

### JVM Tuning

#### Memory Configuration
```bash
# Small repositories (<100 commits)
java -Xms64m -Xmx128m -jar rmi.jar

# Medium repositories (100-1000 commits)
java -Xms128m -Xmx256m -jar rmi.jar

# Large repositories (>1000 commits)
java -Xms256m -Xmx512m -jar rmi.jar
```

#### Garbage Collection
```bash
# Use G1GC for better latency
java -XX:+UseG1GC -Xmx256m -jar rmi.jar

# Enable GC logging
java -Xlog:gc*:file=gc.log -jar rmi.jar
```

#### JIT Compilation
```bash
# Aggressive optimization
java -XX:+AggressiveOpts -jar rmi.jar

# C2 compiler only (for throughput)
java -XX:-TieredCompilation -jar rmi.jar
```

### Network Optimization

#### Connection Pooling
OkHttp connection pooling configured in code:
```java
ConnectionPool pool = new ConnectionPool(5, 5, TimeUnit.MINUTES);
OkHttpClient client = new OkHttpClient.Builder()
    .connectionPool(pool)
    .build();
```

#### Timeouts
```java
OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .build();
```

### Docker Performance

#### Resource Limits
```yaml
services:
  rmi:
    image: rmi-app
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 256M
        reservations:
          cpus: '0.5'
          memory: 128M
```

#### Volume Mounts for Caching
```bash
docker run --rm \
  -v $HOME/.rmi-cache:/cache \
  -e CACHE_DIR=/cache \
  rmi-app analyze owner/repo
```

---

## Maintenance Procedures

### Planned Maintenance Window

#### Before Maintenance
1. **Notify users** - 48 hours advance notice
2. **Create maintenance branch** - `git checkout -b maintenance-YYYYMMDD`
3. **Backup current state** - Copy JAR and configuration
4. **Schedule maintenance window** - Low-traffic period (e.g., weekends)

#### During Maintenance
1. **Stop service** - `pkill -f repo-maintainability-index`
2. **Apply updates** - Deploy new version
3. **Run smoke tests** - Verify basic functionality
4. **Monitor metrics** - Watch for anomalies

#### After Maintenance
1. **Verify functionality** - Run comprehensive tests
2. **Monitor errors** - Check logs for issues
3. **Update status** - Mark maintenance complete
4. **Post-mortem** - Document changes and issues

### Dependency Updates

#### Quarterly Dependency Audit
```bash
# 1. Check for updates
mvn versions:display-dependency-updates

# 2. Check for vulnerabilities
mvn org.owasp:dependency-check-maven:check

# 3. Update dependencies
mvn versions:use-latest-versions

# 4. Run tests
mvn clean verify

# 5. Deploy if all tests pass
```

### Log Rotation

#### Configure Logback Rotation
```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/rmi.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/rmi.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>30</maxHistory>
        <totalSizeCap>1GB</totalSizeCap>
    </rollingPolicy>
</appender>
```

### Backup Procedures

#### Configuration Backup
```bash
# Backup environment configuration
cp .env .env.backup.$(date +%Y%m%d)

# Backup logs (if file logging enabled)
tar -czf logs-backup-$(date +%Y%m%d).tar.gz logs/
```

#### Recovery Procedures
```bash
# Restore configuration
cp .env.backup.20240101 .env

# Restore previous JAR version
cp backup/repo-maintainability-index-<version>.jar target/
```

---

## Emergency Contacts

### On-Call Rotation
| Role | Primary | Backup | Contact |
|------|---------|--------|---------|
| Platform Engineer | [Name] | [Name] | Slack: @engineer |
| DevOps Lead | [Name] | [Name] | Slack: @lead |
| Security Team | [Team] | N/A | Slack: #security |

### Escalation Path
```
Tier 1: On-Call Engineer (15 min response)
    │
    ├─ If unresolved after 30 min
    ▼
Tier 2: DevOps Lead (30 min response)
    │
    ├─ If critical issue persists
    ▼
Tier 3: Engineering Manager
    │
    ├─ If requires business decision
    ▼
Tier 4: VP Engineering
```

### External Dependencies

#### GitHub Support
- **Status**: https://www.githubstatus.com/
- **Support**: https://support.github.com/
- **API Docs**: https://docs.github.com/en/rest

#### OpenRouter Support
- **Status**: https://status.openrouter.ai/
- **Support**: https://openrouter.ai/docs
- **Discord**: https://discord.gg/openrouter

---

## Quick Reference Commands

### Health Checks
```bash
# Version check
java -jar rmi.jar --version

# GitHub API test
curl -H "Authorization: Bearer $GITHUB_TOKEN" https://api.github.com/rate_limit

# OpenRouter API test
curl -X POST https://openrouter.ai/api/v1/chat/completions \
  -H "Authorization: Bearer $OPENROUTER_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"openai/gpt-3.5-turbo","messages":[{"role":"user","content":"test"}]}'
```

### Troubleshooting
```bash
# Enable debug logging
export LOG_LEVEL=DEBUG
java -jar rmi.jar analyze owner/repo

# Test without LLM
java -jar rmi.jar analyze owner/repo

# Test with different model
java -jar rmi.jar analyze owner/repo --llm --model openai/gpt-3.5-turbo

# JSON output for programmatic parsing
java -jar rmi.jar analyze owner/repo --format json
```

### Docker Operations
```bash
# Run container
docker run --rm -e GITHUB_TOKEN=$GITHUB_TOKEN rmi-app analyze owner/repo

# Interactive shell
docker run -it --entrypoint /bin/sh rmi-app

# View logs
docker logs <container_id>

# Inspect container
docker inspect rmi-app
```

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Next Review:** Quarterly  
**Owner:** DevOps/Platform Engineering Team
