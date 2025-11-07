# Architecture Decision Records (ADR)

This directory contains Architecture Decision Records for the Repository Maintainability Index project.

## What is an ADR?

An Architecture Decision Record (ADR) captures an important architectural decision made along with its context and consequences.

## Format

Each ADR follows this structure:
- **Status**: Proposed, Accepted, Deprecated, Superseded
- **Context**: What is the issue we're seeing that is motivating this decision?
- **Decision**: What is the change that we're proposing and/or doing?
- **Consequences**: What becomes easier or more difficult to do because of this change?

## Index

| ADR | Title | Status |
|-----|-------|--------|
| [ADR-001](ADR-001-monolithic-cli-architecture.md) | Monolithic CLI Architecture | Accepted |
| [ADR-002](ADR-002-github-api-client-library.md) | Direct GitHub API Client vs Library | Accepted |
| [ADR-003](ADR-003-weighted-metrics-system.md) | Weighted Metrics System | Accepted |
| [ADR-004](ADR-004-optional-llm-integration.md) | Optional LLM Integration | Accepted |
| [ADR-005](ADR-005-immutable-domain-models.md) | Immutable Domain Models | Accepted |

## Creating New ADRs

When making significant architectural decisions:

1. Create a new file: `ADR-XXX-descriptive-title.md`
2. Use the template from existing ADRs
3. Get review from team/maintainers
4. Update this index
