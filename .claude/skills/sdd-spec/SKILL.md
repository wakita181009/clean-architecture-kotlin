---
name: sdd-spec
description: >
  SDD (Specification-Driven Development) for Kotlin CA/FP projects. Produces precise,
  layer-aware spec files that drive tests and implementation. Use when planning a feature,
  designing the domain model, specifying API contracts, or writing acceptance criteria.
  Triggers on: spec writing, feature planning, domain model design, error hierarchy design.
---

# SDD Spec

Save specs to `.claude/specs/[feature-name].md`.

See [references/spec-template.md](references/spec-template.md) for the full format.

## Core Rules

- **Errors before happy path** — list all error cases before describing success paths
- **No implementation details** — specify WHAT the system does, not HOW
- **Testable criteria** — every acceptance criterion must map to a concrete test case
- **Layer ownership** — for every error, identify which layer detects it (domain validation / application logic / infra)
- **Ubiquitous language** — use consistent domain terms throughout; the spec is the shared vocabulary

## Spec Sections

1. Context — why this feature exists
2. Ubiquitous Language — term definitions
3. Domain Model — entities, value objects, errors, repository interface changes
4. Use Cases — input (primitive types), happy path, all error cases, application error types
5. Infrastructure — DB schema, migration file name
6. Presentation — REST/GraphQL contracts, HTTP status mapping per error variant
7. Acceptance Criteria — given/when/then, one per behavior

## Error Hierarchy

- `sealed interface XxxError : DomainError` — domain validation failures, not found
- `sealed interface XxxUseCaseError : ApplicationError` — wraps domain errors per use case
