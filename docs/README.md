# Documentation Index

Welcome to the Repository Maintainability Index documentation.

## 📚 Table of Contents

### For Users
- [Main README](../README.md) - Getting started, usage, and features
- [Security Best Practices](../SECURITY_BEST_PRACTICES.md) - API key management
- [LLM Features](../LLM_FEATURES.md) - AI-powered analysis guide

### For Contributors
- [Implementation Notes](IMPLEMENTATION_NOTES.md) - **START HERE** - Overview of the modernization strategy
- [Architecture Documentation](architecture/README.md) - System design overview
- [C4 Architecture Diagrams](architecture/C4_ARCHITECTURE.md) - Visual architecture
- [Architecture Decision Records](architecture/adr/README.md) - Key decisions and rationale
- [Modernization Roadmap](MODERNIZATION_ROADMAP.md) - Current and future work

### For Decision Makers
- [Implementation Notes](IMPLEMENTATION_NOTES.md) - Overview of the implementation strategy
- [Testing Documentation](TESTING_VERIFICATION.md) - Comprehensive testing results

## 🎯 Quick Start

### "How is this project architected?"
**Read:** [C4 Architecture](architecture/C4_ARCHITECTURE.md)  
**TL;DR:** Monolithic CLI with clean layered architecture.

### "Why was X decision made?"
**Read:** [ADR Index](architecture/adr/README.md)  
**Available ADRs:**
- ADR-001: Monolithic CLI Architecture
- ADR-002: Direct GitHub API Client
- ADR-003: Weighted Metrics System
- ADR-004: Optional LLM Integration
- ADR-005: Immutable Domain Models

### "What's been implemented and what's next?"
**Read:** [Modernization Roadmap](MODERNIZATION_ROADMAP.md)  
**Status:** Architecture docs and enhanced CI/CD complete. Optional enhancements available.

## 📊 Key Documents Summary

| Document | Purpose | Audience | Priority |
|----------|---------|----------|----------|
| [Implementation Notes](IMPLEMENTATION_NOTES.md) | What we did and why | All | 🔴 Must Read |
| [C4 Architecture](architecture/C4_ARCHITECTURE.md) | System design and structure | Developers | 🟡 Important |
| [ADR Index](architecture/adr/README.md) | Design decisions with rationale | Developers | 🟡 Important |
| [Modernization Roadmap](MODERNIZATION_ROADMAP.md) | Current status and future work | All | 🟢 Reference |
| [Testing Documentation](TESTING_VERIFICATION.md) | Test results and coverage | QA / Developers | 🟢 Reference |

## 🏗️ Architecture at a Glance

```
┌─────────────────────────────────────┐
│            CLI Layer                │  (User Interface)
│         (Picocli)                   │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│         Service Layer               │  (Business Logic)
│      (AnalysisService)              │
└──┬──────────────┬──────────────┬───┘
   │              │              │
   ▼              ▼              ▼
┌─────────┐  ┌─────────┐  ┌─────────┐
│Metrics  │  │ GitHub  │  │  LLM    │  (Data Sources)
│Layer    │  │ Client  │  │ Client  │
└─────────┘  └─────────┘  └─────────┘
```

**Key Principles:**
- Clean separation of concerns
- Dependency injection throughout
- Immutable domain models
- Interface-based design
- Fail-safe defaults

## 🔍 Finding Information

### I want to understand...

**...why this isn't using microservices**  
→ [ADR-001: Monolithic CLI Architecture](architecture/adr/ADR-001-monolithic-cli-architecture.md)

**...how GitHub API integration works**  
→ [ADR-002: Direct GitHub API Client](architecture/adr/ADR-002-github-api-client-library.md)  
→ [C4 Architecture: GitHub Client](architecture/C4_ARCHITECTURE.md#github-client)

**...how metrics are weighted**  
→ [ADR-003: Weighted Metrics System](architecture/adr/ADR-003-weighted-metrics-system.md)

**...how LLM integration works**  
→ [ADR-004: Optional LLM Integration](architecture/adr/ADR-004-optional-llm-integration.md)  
→ [LLM Features Guide](../LLM_FEATURES.md)

**...why models are immutable**  
→ [ADR-005: Immutable Domain Models](architecture/adr/ADR-005-immutable-domain-models.md)

**...what's implemented and what's not**  
→ [Implementation Notes](IMPLEMENTATION_NOTES.md)  
→ [Modernization Roadmap](MODERNIZATION_ROADMAP.md)

## 🚀 Getting Started (Different Roles)

### As a User
1. Read [Main README](../README.md)
2. Follow installation instructions
3. Run your first analysis
4. Optionally: Try [LLM features](../LLM_FEATURES.md)

### As a Contributor
1. Read [Implementation Notes](IMPLEMENTATION_NOTES.md) - Understand the strategy
2. Review [Architecture](architecture/C4_ARCHITECTURE.md) - Understand structure
3. Browse [ADRs](architecture/adr/README.md) - Understand decisions
4. Clone and build: `mvn clean package`
5. Run tests: `mvn test`

### As a Decision Maker
1. Review [Implementation Notes](IMPLEMENTATION_NOTES.md) - What's been done
2. Check [Testing Documentation](TESTING_VERIFICATION.md) - Quality metrics
3. Review [Modernization Roadmap](MODERNIZATION_ROADMAP.md) - Future work
4. Make informed decision based on context

## 📝 Creating New Documentation

### Adding an ADR
1. Copy template from existing ADR
2. Fill in: Status, Context, Decision, Consequences
3. Add to [ADR Index](architecture/adr/README.md)
4. Get reviewed

### Updating Architecture Docs
1. Update [C4 Architecture](architecture/C4_ARCHITECTURE.md) if structure changes
2. Create ADR for significant architectural decisions
3. Update this index if adding new documents

## 🔒 Security

For security-related documentation:
- [Security Best Practices](../SECURITY_BEST_PRACTICES.md)
- [Security Fix History](../SECURITY_FIX.md)

## 📈 Quality & CI/CD

- **CI Pipeline:** [.github/workflows/ci.yml](../.github/workflows/ci.yml)
- **Coverage:** Target: 90% instruction, 85% branch
- **Security Scanning:** OWASP Dependency-Check, Trivy
- **SBOM:** CycloneDX format, generated on build

## 🤝 Contributing

When making significant changes:
1. Review relevant ADRs
2. Create new ADR if decision is significant
3. Update architecture docs if structure changes
4. Update this index if adding documentation

## 📞 Questions?

- **Architecture questions:** Review ADRs and C4 diagrams
- **Implementation questions:** Check Implementation Notes
- **Testing questions:** Check Testing Documentation
- **Future work questions:** Check Modernization Roadmap

---

**Last Updated:** 2024  
**Maintainers:** Repository Maintainability Index Team
