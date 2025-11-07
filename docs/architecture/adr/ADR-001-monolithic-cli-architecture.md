# ADR-001: Monolithic CLI Architecture

## Status

**Accepted** (2024)

## Context

We need to build a GitHub repository quality analysis tool. There are several architectural approaches we could take:

1. **Monolithic CLI application** - Single executable JAR with all functionality
2. **Microservices** - Multiple services (API gateway, analysis service, storage service, etc.)
3. **Web service with API** - REST API with web UI
4. **Plugin-based architecture** - Core + pluggable metric calculators

The project requirements are:
- Command-line tool for developer use
- Analyzes GitHub repositories on-demand
- Outputs results to console or JSON
- No persistent storage required
- Single-user, sequential execution

## Decision

We will implement a **monolithic CLI architecture** using a single executable JAR file.

### Key Characteristics:
- Single process, single JVM
- All dependencies bundled via Maven Shade plugin
- No external services or databases
- Stateless execution (no session management)
- Sequential processing (no concurrency)

### Internal Structure:
- **Clean layered architecture** with clear boundaries:
  - CLI Layer (Picocli)
  - Service Layer (orchestration)
  - Metrics Layer (calculators)
  - Client Layer (GitHub API, LLM API)
  - Model Layer (domain objects)

## Alternatives Considered

### 1. Microservices Architecture
**Rejected because:**
- Massive overkill for a CLI tool
- Would require container orchestration (Kubernetes)
- Adds network latency and complexity
- No need for independent scaling
- No need for polyglot services

### 2. Web Service with API
**Rejected because:**
- Not a requirement (CLI tool specified)
- Would require hosting infrastructure
- Adds authentication/authorization complexity
- More complex deployment
- Could be added later if needed

### 3. Plugin-based Architecture
**Rejected because:**
- Over-engineering for 6 known metrics
- Adds classloader complexity
- Harder to test and debug
- No requirement for third-party plugins
- Can refactor later if extensibility becomes critical

## Consequences

### Positive:
✅ **Simplicity**: Single JAR, easy to distribute and run
✅ **Fast startup**: No network calls to other services
✅ **Easy testing**: All code in one codebase
✅ **Low operational complexity**: No deployment infrastructure needed
✅ **Predictable performance**: No network latency between components
✅ **Easy debugging**: Single process to debug
✅ **Version consistency**: All components versioned together

### Negative:
❌ **No horizontal scaling**: Can't scale out (but not needed for CLI)
❌ **No language polyglot**: Everything in Java (acceptable trade-off)
❌ **Longer build times**: All code built together (mitigated by Maven caching)
❌ **JAR size**: ~20MB with all dependencies (acceptable for modern systems)

### Neutral:
⚠️ **Extension path**: If we need web service later, we can extract service layer as separate module
⚠️ **Metrics addition**: New metrics require code change and rebuild (acceptable)

## Implementation Notes

- Use Maven Shade plugin for uber-JAR creation
- Main class: `com.kaicode.rmi.Main`
- Picocli handles CLI parsing and help generation
- Dependency injection via constructor (no DI framework needed)
- Clear package boundaries enforce layering

## Related Decisions

- [ADR-002: Direct GitHub API Client](ADR-002-github-api-client-library.md)
- [ADR-003: Weighted Metrics System](ADR-003-weighted-metrics-system.md)

## References

- [Monolith First](https://martinfowler.com/bliki/MonolithFirst.html) by Martin Fowler
- [The Majestic Monolith](https://m.signalvnoise.com/the-majestic-monolith/) by DHH
