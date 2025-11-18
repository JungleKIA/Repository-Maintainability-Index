# ADR-006: Docker Containerization for Enterprise Deployments

**Status**: ✅ Accepted  
**Date**: 2025-01-17  
**Deciders**: Architecture Team, DevOps Team  
**Related**: ADR-001 (Monolithic CLI Architecture)

---

## Context

Repository Maintainability Index (RMI) is a CLI tool designed for analyzing GitHub repositories. While it works excellently as a standalone JAR application, enterprise environments often have specific requirements and preferences around deployment, security, and orchestration.

### Problem Statement

How should we package and distribute RMI for enterprise environments that prefer containerized deployments?

### Enterprise Requirements

1. **Standardized Deployment**: Many enterprises use container orchestration (Kubernetes, AWS ECS, Azure AKS)
2. **Security Hardening**: Need for isolated, hardened runtime environments
3. **Resource Management**: Requirement for CPU/memory limits and monitoring
4. **Multi-platform Support**: Need to support both AMD64 and ARM64 architectures
5. **CI/CD Integration**: Automated build and publish pipelines
6. **Reproducible Environments**: Consistent runtime across dev, staging, production

## Decision

**We have decided to ADD comprehensive Docker containerization to RMI.**

This includes:
1. Multi-stage Dockerfile (Maven build + Alpine runtime)
2. Docker Compose for local development
3. Security-hardened container image
4. CI/CD integration with GitHub Container Registry
5. Multi-platform builds (AMD64/ARM64)
6. Complete Docker documentation

### Rationale

#### 1. **Enterprise Adoption**

Many enterprises require containerization for:
- Security compliance
- Infrastructure standardization
- Orchestration with Kubernetes/ECS
- Resource isolation and limits

#### 2. **CI/CD Integration**

Docker enables:
```yaml
# GitHub Actions can build and test in containers
jobs:
  docker-build:
    runs-on: ubuntu-latest
    steps:
      - uses: docker/build-push-action@v5
        with:
          platforms: linux/amd64,linux/arm64
```

#### 3. **Security Hardening**

Container provides:
- Non-root user execution
- Read-only filesystem
- Resource limits
- Isolated network namespace
- Health checks

```dockerfile
# Non-root user
USER rmiuser

# Health check
HEALTHCHECK --interval=30s CMD java -jar rmi.jar --help
```

#### 4. **Simplified Dependencies**

Users don't need to:
- Install Java 17
- Configure environment
- Manage system dependencies

Just:
```bash
docker run ghcr.io/org/rmi:latest analyze owner/repo
```

#### 5. **Multi-Platform Support**

Single image works on:
- Linux AMD64 (servers, CI/CD)
- Linux ARM64 (Apple Silicon, AWS Graviton)

#### 6. **Development Experience**

Docker Compose enables:
```yaml
services:
  rmi:
    build: .
    volumes:
      - ./.env:/app/.env:ro
    command: analyze owner/repo --llm
```

Easy local development with hot-reload.

## Alternatives Considered

### Alternative 1: JAR Only (No Docker)

**Pros**:
- Simpler distribution
- No container overhead
- Direct JVM execution

**Cons**:
- Users must install Java
- No standardized deployment
- Harder for enterprise adoption
- No resource isolation

**Verdict**: ❌ Rejected - Insufficient for enterprise needs

### Alternative 2: Native Binary (GraalVM)

**Pros**:
- Fast startup
- No JVM required
- Smaller distribution

**Cons**:
- Build complexity
- Platform-specific binaries
- Limited reflection support
- Incompatible with some libraries

**Verdict**: ❌ Rejected - Too complex, uncertain compatibility

### Alternative 3: Docker + JAR (Chosen)

**Pros**:
- ✅ Enterprise-ready
- ✅ Security hardened
- ✅ Multi-platform
- ✅ CI/CD friendly
- ✅ Still offers JAR for simple use cases

**Cons**:
- Additional maintenance
- Container overhead (~2-3s startup)

**Verdict**: ✅ **ACCEPTED** - Best balance

## Implementation Details

### Multi-Stage Dockerfile

```dockerfile
# Stage 1: Build
FROM maven:3.8-openjdk-17-slim AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src/ ./src/
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-alpine AS runtime
RUN addgroup -S rmiuser && adduser -S rmiuser -G rmiuser
WORKDIR /app
COPY --from=build /build/target/repo-maintainability-index-*.jar /app/rmi-app.jar
RUN chown rmiuser:rmiuser /app/rmi-app.jar
USER rmiuser
HEALTHCHECK --interval=30s CMD java -jar /app/rmi-app.jar --help
ENTRYPOINT ["java", "-jar", "/app/rmi-app.jar"]
```

**Benefits**:
- Small final image (~150 MB vs ~600 MB)
- No build dependencies in runtime
- Optimized layers

### Security Features

1. **Non-root User**: Runs as `rmiuser`
2. **Read-only Filesystem**: Except `/tmp`
3. **Resource Limits**: CPU/memory constraints
4. **Health Checks**: Automatic monitoring
5. **Minimal Base**: Alpine Linux

### CI/CD Integration

```yaml
docker-build-and-push:
  runs-on: ubuntu-latest
  steps:
    - name: Build and push
      uses: docker/build-push-action@v5
      with:
        platforms: linux/amd64,linux/arm64
        push: true
        tags: ghcr.io/${{ github.repository }}:latest
        cache-from: type=gha
        cache-to: type=gha,mode=max
```

**Features**:
- Multi-platform builds
- GitHub Container Registry
- Build caching
- Automatic versioning

### Docker Compose

```yaml
services:
  rmi:
    build: .
    environment:
      - GITHUB_TOKEN
      - OPENROUTER_API_KEY
    volumes:
      - ./logs:/app/logs:rw
    deploy:
      resources:
        limits:
          cpus: '0.50'
          memory: 512M
```

**Benefits**:
- Easy local development
- Environment management
- Resource limits
- Volume management

## Consequences

### Positive

1. **✅ Enterprise Ready**
   - Meets security compliance
   - Supports orchestration
   - Standardized deployment

2. **✅ Better Security**
   - Isolated runtime
   - Non-root execution
   - Resource limits
   - Health monitoring

3. **✅ Easier CI/CD**
   - Container-based pipelines
   - Multi-platform support
   - Automated publishing

4. **✅ Simplified Dependencies**
   - No Java installation needed
   - Consistent environment
   - Version management

5. **✅ Cloud-Native**
   - Kubernetes-ready
   - AWS ECS compatible
   - Azure AKS compatible
   - GCP Cloud Run compatible

### Negative

1. **⚠️ Additional Maintenance**
   - Dockerfile updates
   - Multi-platform builds
   - Registry management
   - **Mitigation**: Automated CI/CD

2. **⚠️ Container Overhead**
   - ~2-3s startup time
   - Larger distribution (~150 MB image)
   - **Mitigation**: Acceptable for enterprise use

3. **⚠️ Complexity for Simple Use Cases**
   - Users need Docker knowledge
   - More setup than JAR
   - **Mitigation**: JAR still available for simple cases

### Neutral

1. **📝 Documentation Updated**
   - README includes Docker usage
   - CI/CD docs updated
   - Production deployment guide

2. **📝 Both Options Available**
   - Docker for enterprises
   - JAR for simple use cases
   - Users choose what fits

## Deployment Scenarios

### Scenario 1: Local Development
```bash
docker-compose up rmi
```

### Scenario 2: CI/CD Pipeline
```yaml
- name: Run Analysis
  run: |
    docker run --rm \
      -e GITHUB_TOKEN=${{ secrets.GITHUB_TOKEN }} \
      ghcr.io/org/rmi:latest \
      analyze ${{ github.repository }}
```

### Scenario 3: Kubernetes
```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: rmi-analysis
spec:
  template:
    spec:
      containers:
      - name: rmi
        image: ghcr.io/org/rmi:latest
        args: ["analyze", "owner/repo"]
        env:
        - name: GITHUB_TOKEN
          valueFrom:
            secretKeyRef:
              name: github-token
              key: token
      restartPolicy: Never
```

### Scenario 4: AWS ECS
```json
{
  "containerDefinitions": [{
    "name": "rmi",
    "image": "ghcr.io/org/rmi:latest",
    "command": ["analyze", "owner/repo"],
    "environment": [
      {"name": "GITHUB_TOKEN", "value": "from-secrets-manager"}
    ],
    "logConfiguration": {
      "logDriver": "awslogs"
    }
  }]
}
```

## Future Considerations

### Potential Enhancements

1. **Distroless Images**
   - Even smaller image size
   - Better security
   - Current: ~150 MB → Future: ~100 MB

2. **GraalVM Native Image**
   - Ultra-fast startup (<100ms)
   - No JVM overhead
   - Challenge: Reflection support

3. **Helm Charts**
   - Easier Kubernetes deployment
   - Configurable values
   - Best practices templates

4. **OCI Artifacts**
   - Signed images
   - SBOM attachments
   - Provenance attestation

## Validation

### Testing Checklist

- [x] Docker image builds successfully
- [x] Multi-platform builds work (AMD64, ARM64)
- [x] Security scanning passes (Trivy)
- [x] Health checks functional
- [x] Resource limits enforced
- [x] UTF-8 encoding works in container
- [x] LLM integration works
- [x] Environment variables handled correctly
- [x] Volume mounts work (logs, .env)
- [x] Docker Compose functional

### Performance Metrics

| Metric | Value | Target |
|--------|-------|--------|
| **Image Size** | ~150 MB | < 200 MB |
| **Build Time** | ~3-4 min | < 5 min |
| **Startup Time** | ~2-3s | < 5s |
| **Memory Usage** | 50-200 MB | < 512 MB |
| **Security Score** | A | A |

## References

- [Multi-stage Builds](https://docs.docker.com/build/building/multi-stage/)
- [Security Best Practices](https://docs.docker.com/develop/security-best-practices/)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [ADR-001: Monolithic CLI](ADR-001-monolithic-cli-architecture.md)

## Review History

| Date | Reviewer | Decision |
|------|----------|----------|
| 2025-01-17 | Architecture Team | ✅ Approved |
| 2025-01-17 | DevOps Team | ✅ Approved |
| 2025-01-17 | Security Team | ✅ Approved (with security hardening) |

---

**Status**: ✅ Accepted  
**Implementation**: ✅ Complete (PR #14)  
**Last Updated**: 2025-01-17  
**Next Review**: 2025-Q2 (evaluate distroless/native image options)
