# Monitoring & Observability Setup Guide

**Application:** Repository Maintainability Index (RMI)  
**Version:** 1.0.0  
**Last Updated:** November 18, 2025

---

## Table of Contents

1. [Overview](#overview)
2. [Observability Strategy](#observability-strategy)
3. [Metrics Implementation](#metrics-implementation)
4. [Logging Configuration](#logging-configuration)
5. [Distributed Tracing](#distributed-tracing)
6. [Health Checks](#health-checks)
7. [Alerting](#alerting)
8. [Dashboard Setup](#dashboard-setup)
9. [Implementation Roadmap](#implementation-roadmap)

---

## Overview

This guide provides comprehensive instructions for implementing production-grade observability for RMI. Currently, RMI uses basic SLF4J/Logback logging. This document outlines the path to enterprise-level monitoring.

### Current State
- ✅ Basic console logging (SLF4J + Logback)
- ❌ No metrics instrumentation
- ❌ No distributed tracing
- ❌ No structured logging
- ❌ No health check endpoints
- ❌ No error tracking integration

### Target State
- ✅ Prometheus metrics (request rates, latencies, errors)
- ✅ Structured JSON logging with correlation IDs
- ✅ Distributed tracing (OpenTelemetry → Jaeger)
- ✅ Health check endpoints
- ✅ Grafana dashboards
- ✅ Sentry error tracking
- ✅ Automated alerting (PagerDuty/Slack)

---

## Observability Strategy

### Three Pillars of Observability

```
┌─────────────────────────────────────────────────────────┐
│                   OBSERVABILITY                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌───────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │  METRICS  │  │   LOGS    │  │     TRACES       │  │
│  │           │  │           │  │                  │  │
│  │ What is   │  │ What      │  │ Why is it       │  │
│  │ happening?│  │ happened? │  │ happening?      │  │
│  │           │  │           │  │                  │  │
│  │ Prometheus│  │ ELK/      │  │ Jaeger/         │  │
│  │ Grafana   │  │ Splunk    │  │ OpenTelemetry   │  │
│  └───────────┘  └───────────┘  └──────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Key Metrics to Track

#### Application Metrics
- **Throughput**: Analyses per minute/hour
- **Latency**: Analysis duration (p50, p95, p99)
- **Error Rate**: Failed analyses percentage
- **Success Rate**: Successful analyses percentage

#### External Dependencies
- **GitHub API**: Request count, latency, rate limit consumption, errors
- **OpenRouter API**: Request count, latency, token usage, errors
- **Network**: Connection timeouts, DNS resolution time

#### Resource Metrics
- **JVM**: Heap usage, GC frequency, thread count
- **System**: CPU usage, memory, disk I/O

---

## Metrics Implementation

### Step 1: Add Micrometer Dependency

Add to `pom.xml`:

```xml
<dependencies>
    <!-- Micrometer Core -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-core</artifactId>
        <version>1.12.0</version>
    </dependency>
    
    <!-- Micrometer Prometheus Registry -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
        <version>1.12.0</version>
    </dependency>
    
    <!-- Optional: JVM Metrics -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-core</artifactId>
        <version>1.12.0</version>
    </dependency>
</dependencies>
```

### Step 2: Create Metrics Registry

**File:** `src/main/java/com/kaicode/rmi/observability/MetricsRegistry.java`

```java
package com.kaicode.rmi.observability;

import io.micrometer.core.instrument.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class MetricsRegistry {
    private static final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    
    // Counters
    public static final Counter analysisTotal = registry.counter("rmi.analysis.total");
    public static final Counter analysisSuccess = registry.counter("rmi.analysis.success");
    public static final Counter analysisFailure = registry.counter("rmi.analysis.failure");
    
    // Timers
    public static final Timer analysisLatency = registry.timer("rmi.analysis.latency");
    public static final Timer githubApiLatency = registry.timer("rmi.github.api.latency");
    public static final Timer llmApiLatency = registry.timer("rmi.llm.api.latency");
    
    // Gauges
    public static final AtomicInteger githubRateLimitRemaining = registry.gauge(
        "rmi.github.rate_limit.remaining",
        new AtomicInteger(5000)
    );
    
    public static PrometheusMeterRegistry getRegistry() {
        return registry;
    }
    
    public static String scrape() {
        return registry.scrape();
    }
}
```

### Step 3: Instrument Code

**Example: GitHubClient.java**

```java
public class GitHubClient {
    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);
    
    public RepositoryInfo fetchRepository(String owner, String repo) {
        // Start timer
        Timer.Sample sample = Timer.start(MetricsRegistry.getRegistry());
        
        try {
            // Make API call
            Response response = httpClient.newCall(request).execute();
            
            // Record success
            MetricsRegistry.githubApiLatency.record(() -> {
                // API call logic
            });
            
            // Update rate limit gauge
            String remaining = response.header("X-RateLimit-Remaining");
            if (remaining != null) {
                MetricsRegistry.githubRateLimitRemaining.set(Integer.parseInt(remaining));
            }
            
            sample.stop(MetricsRegistry.githubApiLatency);
            return parseResponse(response);
            
        } catch (Exception e) {
            sample.stop(MetricsRegistry.githubApiLatency);
            logger.error("GitHub API error", e);
            throw new GitHubApiException("Failed to fetch repository", e);
        }
    }
}
```

**Example: MaintainabilityService.java**

```java
public class MaintainabilityService {
    
    public MaintainabilityReport analyze(String owner, String repo) {
        MetricsRegistry.analysisTotal.increment();
        
        Timer.Sample sample = Timer.start(MetricsRegistry.getRegistry());
        
        try {
            // Perform analysis
            MaintainabilityReport report = performAnalysis(owner, repo);
            
            // Record success
            MetricsRegistry.analysisSuccess.increment();
            sample.stop(MetricsRegistry.analysisLatency);
            
            return report;
            
        } catch (Exception e) {
            MetricsRegistry.analysisFailure.increment();
            sample.stop(MetricsRegistry.analysisLatency);
            throw e;
        }
    }
}
```

### Step 4: Expose Metrics Endpoint (Optional)

For containerized deployments, expose metrics via HTTP:

**File:** `src/main/java/com/kaicode/rmi/observability/MetricsServer.java`

```java
package com.kaicode.rmi.observability;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MetricsServer {
    private final HttpServer server;
    
    public MetricsServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/metrics", exchange -> {
            String response = MetricsRegistry.scrape();
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
    }
    
    public void start() {
        server.start();
    }
    
    public void stop() {
        server.stop(0);
    }
}
```

### Step 5: Prometheus Configuration

**File:** `prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'rmi'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/metrics'
```

### Prometheus Queries

```promql
# Analysis success rate (last 5 minutes)
rate(rmi_analysis_success_total[5m]) / rate(rmi_analysis_total[5m]) * 100

# p95 analysis latency
histogram_quantile(0.95, rate(rmi_analysis_latency_bucket[5m]))

# GitHub rate limit consumption
(5000 - rmi_github_rate_limit_remaining) / 5000 * 100

# Error rate
rate(rmi_analysis_failure_total[5m]) / rate(rmi_analysis_total[5m]) * 100

# GitHub API latency p99
histogram_quantile(0.99, rate(rmi_github_api_latency_bucket[5m]))
```

---

## Logging Configuration

### Step 1: Structured Logging with Logback

**File:** `src/main/resources/logback.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Console Appender (Development) -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    
    <!-- JSON File Appender (Production) -->
    <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/rmi.json</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/rmi.%d{yyyy-MM-dd}.json</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>correlationId</includeMdcKeyName>
            <includeMdcKeyName>repository</includeMdcKeyName>
            <includeMdcKeyName>operation</includeMdcKeyName>
        </encoder>
    </appender>
    
    <!-- Async Appender for Performance -->
    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <appender-ref ref="JSON_FILE" />
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="ASYNC" />
    </root>
</configuration>
```

### Step 2: Add Logstash Encoder Dependency

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### Step 3: Structured Logging with MDC

**Example: Add correlation ID**

```java
import org.slf4j.MDC;

public class MaintainabilityService {
    private static final Logger logger = LoggerFactory.getLogger(MaintainabilityService.class);
    
    public MaintainabilityReport analyze(String owner, String repo) {
        // Generate correlation ID
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        MDC.put("repository", owner + "/" + repo);
        MDC.put("operation", "analyze");
        
        try {
            logger.info("Starting repository analysis");
            
            // Perform analysis
            MaintainabilityReport report = performAnalysis(owner, repo);
            
            logger.info("Analysis completed successfully", 
                "score", report.getOverallScore(),
                "rating", report.getRating());
            
            return report;
            
        } catch (Exception e) {
            logger.error("Analysis failed", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
```

### Step 4: ELK Stack Configuration

**Filebeat Configuration (`filebeat.yml`):**

```yaml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /var/log/rmi/*.json
    json.keys_under_root: true
    json.add_error_key: true

output.elasticsearch:
  hosts: ["localhost:9200"]
  index: "rmi-logs-%{+yyyy.MM.dd}"

setup.kibana:
  host: "localhost:5601"
```

**Elasticsearch Index Template:**

```json
{
  "index_patterns": ["rmi-logs-*"],
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "@timestamp": { "type": "date" },
      "level": { "type": "keyword" },
      "logger_name": { "type": "keyword" },
      "message": { "type": "text" },
      "correlationId": { "type": "keyword" },
      "repository": { "type": "keyword" },
      "operation": { "type": "keyword" },
      "score": { "type": "float" },
      "rating": { "type": "keyword" }
    }
  }
}
```

---

## Distributed Tracing

### Step 1: Add OpenTelemetry Dependencies

```xml
<dependencies>
    <!-- OpenTelemetry API -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-api</artifactId>
        <version>1.32.0</version>
    </dependency>
    
    <!-- OpenTelemetry SDK -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>1.32.0</version>
    </dependency>
    
    <!-- OpenTelemetry Jaeger Exporter -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-jaeger</artifactId>
        <version>1.32.0</version>
    </dependency>
    
    <!-- OpenTelemetry Instrumentation -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-okhttp-3.0</artifactId>
        <version>1.32.0-alpha</version>
    </dependency>
</dependencies>
```

### Step 2: Initialize OpenTelemetry

**File:** `src/main/java/com/kaicode/rmi/observability/TracingConfig.java`

```java
package com.kaicode.rmi.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

public class TracingConfig {
    private static OpenTelemetry openTelemetry;
    
    public static OpenTelemetry initialize() {
        if (openTelemetry == null) {
            // Create Jaeger exporter
            JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:14250")
                .build();
            
            // Create tracer provider
            SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
                .build();
            
            // Build OpenTelemetry instance
            openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();
        }
        
        return openTelemetry;
    }
    
    public static Tracer getTracer(String instrumentationName) {
        return initialize().getTracer(instrumentationName);
    }
}
```

### Step 3: Instrument Code with Tracing

**Example: Service Layer**

```java
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

public class MaintainabilityService {
    private static final Tracer tracer = TracingConfig.getTracer("MaintainabilityService");
    
    public MaintainabilityReport analyze(String owner, String repo) {
        Span span = tracer.spanBuilder("analyze_repository")
            .setAttribute("repository", owner + "/" + repo)
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            // Fetch repository
            Span fetchSpan = tracer.spanBuilder("fetch_repository").startSpan();
            try (Scope fetchScope = fetchSpan.makeCurrent()) {
                RepositoryInfo repoInfo = githubClient.fetchRepository(owner, repo);
                fetchSpan.setAttribute("stars", repoInfo.getStars());
                fetchSpan.setAttribute("forks", repoInfo.getForks());
            } finally {
                fetchSpan.end();
            }
            
            // Calculate metrics
            Span metricsSpan = tracer.spanBuilder("calculate_metrics").startSpan();
            try (Scope metricsScope = metricsSpan.makeCurrent()) {
                List<MetricResult> metrics = calculateMetrics(repoInfo);
                metricsSpan.setAttribute("metric_count", metrics.size());
            } finally {
                metricsSpan.end();
            }
            
            // Generate report
            MaintainabilityReport report = generateReport(repoInfo, metrics);
            span.setAttribute("overall_score", report.getOverallScore());
            span.setAttribute("rating", report.getRating());
            
            return report;
            
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### Step 4: Jaeger Setup

**Docker Compose:**

```yaml
version: '3'
services:
  jaeger:
    image: jaegertracing/all-in-one:latest
    ports:
      - "5775:5775/udp"
      - "6831:6831/udp"
      - "6832:6832/udp"
      - "5778:5778"
      - "16686:16686"  # UI
      - "14250:14250"  # gRPC
      - "14268:14268"
      - "14269:14269"
      - "9411:9411"
    environment:
      - COLLECTOR_ZIPKIN_HOST_PORT=:9411
```

Access Jaeger UI: `http://localhost:16686`

---

## Health Checks

### Step 1: Create Health Check Endpoint

**File:** `src/main/java/com/kaicode/rmi/observability/HealthCheckServer.java`

```java
package com.kaicode.rmi.observability;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HealthCheckServer {
    private final HttpServer server;
    
    public HealthCheckServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Liveness probe
        this.server.createContext("/health/live", exchange -> {
            String response = "{\"status\":\"UP\",\"checks\":[{\"name\":\"jvm\",\"status\":\"UP\"}]}";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        
        // Readiness probe
        this.server.createContext("/health/ready", exchange -> {
            boolean githubReachable = checkGitHubApi();
            String status = githubReachable ? "UP" : "DOWN";
            int code = githubReachable ? 200 : 503;
            
            String response = String.format(
                "{\"status\":\"%s\",\"checks\":[{\"name\":\"github_api\",\"status\":\"%s\"}]}",
                status, status
            );
            
            exchange.sendResponseHeaders(code, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
    }
    
    private boolean checkGitHubApi() {
        try {
            // Simple connectivity check
            return true; // Implement actual check
        } catch (Exception e) {
            return false;
        }
    }
    
    public void start() {
        server.start();
    }
}
```

### Step 2: Kubernetes Health Check Configuration

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: rmi
spec:
  containers:
  - name: rmi
    image: rmi-app:latest
    ports:
    - containerPort: 8080
    livenessProbe:
      httpGet:
        path: /health/live
        port: 8080
      initialDelaySeconds: 10
      periodSeconds: 30
    readinessProbe:
      httpGet:
        path: /health/ready
        port: 8080
      initialDelaySeconds: 5
      periodSeconds: 10
```

---

## Alerting

### Prometheus AlertManager Rules

**File:** `prometheus-alerts.yml`

```yaml
groups:
  - name: rmi_alerts
    interval: 30s
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: |
          rate(rmi_analysis_failure_total[5m]) / rate(rmi_analysis_total[5m]) > 0.10
        for: 5m
        labels:
          severity: critical
          service: rmi
        annotations:
          summary: "High analysis error rate"
          description: "Error rate is {{ $value | humanizePercentage }} (threshold: 10%)"
          runbook_url: "https://docs.example.com/runbooks/high-error-rate"
      
      # High latency
      - alert: HighLatency
        expr: |
          histogram_quantile(0.95, rate(rmi_analysis_latency_bucket[5m])) > 20
        for: 5m
        labels:
          severity: warning
          service: rmi
        annotations:
          summary: "High analysis latency"
          description: "p95 latency is {{ $value }}s (threshold: 20s)"
      
      # GitHub rate limit high
      - alert: GitHubRateLimitHigh
        expr: rmi_github_rate_limit_remaining < 1000
        labels:
          severity: warning
          service: rmi
        annotations:
          summary: "GitHub API rate limit consumption high"
          description: "Only {{ $value }} requests remaining out of 5000"
      
      # GitHub rate limit critical
      - alert: GitHubRateLimitCritical
        expr: rmi_github_rate_limit_remaining < 100
        labels:
          severity: critical
          service: rmi
        annotations:
          summary: "GitHub API rate limit nearly exhausted"
          description: "Only {{ $value }} requests remaining!"
      
      # Service down
      - alert: ServiceDown
        expr: up{job="rmi"} == 0
        for: 2m
        labels:
          severity: critical
          service: rmi
        annotations:
          summary: "RMI service is down"
          description: "RMI has been down for more than 2 minutes"
```

### AlertManager Configuration

**File:** `alertmanager.yml`

```yaml
global:
  resolve_timeout: 5m

route:
  receiver: 'team-pager'
  group_by: ['alertname', 'severity']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  
  routes:
    - match:
        severity: critical
      receiver: 'pagerduty'
    
    - match:
        severity: warning
      receiver: 'slack'

receivers:
  - name: 'slack'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/WEBHOOK/URL'
        channel: '#rmi-alerts'
        title: '{{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
  
  - name: 'pagerduty'
    pagerduty_configs:
      - service_key: 'YOUR_PAGERDUTY_SERVICE_KEY'
        description: '{{ .GroupLabels.alertname }}'
  
  - name: 'team-pager'
    webhook_configs:
      - url: 'http://your-webhook-endpoint'
```

---

## Dashboard Setup

### Grafana Dashboard JSON

**RMI Overview Dashboard:**

```json
{
  "dashboard": {
    "title": "RMI - Repository Maintainability Index",
    "uid": "rmi-overview",
    "tags": ["rmi", "monitoring"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "Analysis Success Rate (24h)",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(rmi_analysis_success_total[24h]) / rate(rmi_analysis_total[24h]) * 100"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent",
            "thresholds": {
              "steps": [
                { "value": 0, "color": "red" },
                { "value": 90, "color": "yellow" },
                { "value": 95, "color": "green" }
              ]
            }
          }
        }
      },
      {
        "id": 2,
        "title": "Analysis Latency (p95, p99)",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(rmi_analysis_latency_bucket[5m]))",
            "legendFormat": "p95"
          },
          {
            "expr": "histogram_quantile(0.99, rate(rmi_analysis_latency_bucket[5m]))",
            "legendFormat": "p99"
          }
        ]
      },
      {
        "id": 3,
        "title": "GitHub Rate Limit Consumption",
        "type": "gauge",
        "targets": [
          {
            "expr": "(5000 - rmi_github_rate_limit_remaining) / 5000 * 100"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent",
            "thresholds": {
              "steps": [
                { "value": 0, "color": "green" },
                { "value": 70, "color": "yellow" },
                { "value": 90, "color": "red" }
              ]
            }
          }
        }
      },
      {
        "id": 4,
        "title": "Error Rate by Type",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(rmi_errors_total[5m]) by (error_type)"
          }
        ],
        "legend": {
          "show": true
        }
      },
      {
        "id": 5,
        "title": "Throughput (analyses/min)",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(rmi_analysis_total[1m]) * 60"
          }
        ]
      },
      {
        "id": 6,
        "title": "JVM Heap Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area=\"heap\"} / jvm_memory_max_bytes{area=\"heap\"} * 100"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent"
          }
        }
      }
    ]
  }
}
```

### Accessing Grafana

```bash
# Start Grafana with Docker
docker run -d -p 3000:3000 grafana/grafana

# Access UI: http://localhost:3000
# Default credentials: admin/admin
```

### Import Dashboard

1. Navigate to http://localhost:3000
2. Click **+** → **Import**
3. Paste dashboard JSON
4. Select Prometheus data source
5. Click **Import**

---

## Implementation Roadmap

### Phase 1: Foundation (Week 1)
- ✅ Add Micrometer dependency
- ✅ Create MetricsRegistry
- ✅ Instrument core services
- ✅ Set up Prometheus locally
- ✅ Create basic Grafana dashboard

### Phase 2: Structured Logging (Week 2)
- ✅ Add Logstash encoder
- ✅ Implement MDC correlation IDs
- ✅ Configure JSON logging
- ✅ Set up ELK stack (if needed)

### Phase 3: Tracing (Week 3)
- ✅ Add OpenTelemetry dependencies
- ✅ Initialize tracing
- ✅ Instrument GitHubClient, LLMClient
- ✅ Set up Jaeger
- ✅ Verify trace propagation

### Phase 4: Health Checks (Week 4)
- ✅ Create health check endpoints
- ✅ Implement liveness/readiness probes
- ✅ Update Kubernetes manifests
- ✅ Test health checks

### Phase 5: Alerting (Week 5)
- ✅ Define alert rules
- ✅ Configure AlertManager
- ✅ Integrate Slack/PagerDuty
- ✅ Test alert notifications

### Phase 6: Production Rollout (Week 6)
- ✅ Deploy to staging
- ✅ Validate all metrics/logs/traces
- ✅ Load test with monitoring
- ✅ Deploy to production
- ✅ Monitor for 1 week

---

**Document Version:** 1.0  
**Last Updated:** November 18, 2025  
**Next Review:** After Phase 6 completion  
**Owner:** DevOps/SRE Team
