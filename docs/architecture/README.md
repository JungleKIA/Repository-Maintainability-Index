# Architecture Documentation

This directory contains architectural documentation for the Repository Maintainability Index project.

## Contents

- [C4 Architecture Diagrams](C4_ARCHITECTURE.md) - System context and container diagrams
- [Architecture Decision Records](adr/) - Key architectural decisions and their rationale

## Quick Overview

The RMI is a command-line tool following clean architecture principles:

```
┌─────────────────────────────────────────────────────┐
│                    User                             │
│                      │                              │
│                      ▼                              │
│              ┌──────────────┐                       │
│              │  CLI Layer   │  (Picocli)            │
│              └──────┬───────┘                       │
│                     │                               │
│                     ▼                               │
│              ┌──────────────┐                       │
│              │Service Layer │  (Business Logic)     │
│              └──────┬───────┘                       │
│                     │                               │
│         ┌───────────┼───────────┐                   │
│         ▼           ▼           ▼                   │
│    ┌────────┐ ┌─────────┐ ┌─────────┐              │
│    │Metrics │ │ GitHub  │ │   LLM   │              │
│    │ Layer  │ │ Client  │ │ Client  │              │
│    └────────┘ └────┬────┘ └────┬────┘              │
│                    │            │                   │
│                    ▼            ▼                   │
│              ┌──────────────────────┐               │
│              │  External APIs       │               │
│              │  (GitHub, OpenRouter)│               │
│              └──────────────────────┘               │
└─────────────────────────────────────────────────────┘
```

## Architectural Principles

1. **Separation of Concerns**: Clear layer boundaries
2. **Dependency Injection**: Constructor-based DI throughout
3. **Interface-Based Design**: MetricCalculator interface for extensibility
4. **Immutability**: All domain models are immutable
5. **Builder Pattern**: Complex object construction
6. **Fail-Safe Defaults**: Graceful degradation for external API failures

For detailed information, see [C4 Architecture Diagrams](C4_ARCHITECTURE.md).
