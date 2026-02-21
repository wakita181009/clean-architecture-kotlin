---
name: jooq-ddl
description: >
  jOOQ DDL-based code generation and runtime configuration for this Clean Architecture Kotlin project.
  Use when: (1) setting up or updating jOOQ codegen from Flyway SQL migration files,
  (2) troubleshooting identifier case errors ("relation does not exist"),
  (3) writing reactive repository implementations with Flux/Mono + jOOQ,
  (4) configuring JooqConfig DSLContext bean with Settings.
  Covers DDLDatabase Gradle setup, RenderQuotedNames/RenderNameCase Settings,
  and non-blocking Flux.from()/Mono.from() patterns.
---

# jOOQ Setup Guide

## Identifier Case Problem (Common Pitfall)

jOOQ's `DDLDatabase` generates **UPPERCASE** names by default (`DSL.name("GITHUB_REPO")`).
PostgreSQL stores unquoted DDL identifiers as **lowercase** (`github_repo`).
At runtime, jOOQ quotes names → `"GITHUB_REPO"` → PostgreSQL: `relation does not exist`.

Two fixes — prefer the codegen fix:

| Fix | Where | How |
|-----|-------|-----|
| Codegen (preferred) | `build.gradle.kts` | `defaultNameCase = "as_is"` property |
| Runtime fallback | `JooqConfig.kt` | `Settings().withRenderQuotedNames(NEVER).withRenderNameCase(LOWER)` |

See [references/config.md](references/config.md) for codegen, [references/runtime.md](references/runtime.md) for runtime.

## Code Generation

Generates jOOQ DSL classes from Flyway SQL files using `DDLDatabase` — no live DB needed.

| Item | Value |
|------|-------|
| Migration files | `infrastructure/src/main/resources/db/migration/*.sql` |
| Generated output | `infrastructure/build/generated-sources/jooq/main` |
| Module | `infrastructure/build.gradle.kts` |

See [references/config.md](references/config.md) for the full Gradle configuration.

```
./gradlew :infrastructure:jooqCodegen
```

## PostgreSQL-Specific Syntax

The jOOQ DDL parser targets standard SQL — wrap unsupported PostgreSQL syntax:

```sql
-- [jooq ignore start]
CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- [jooq ignore stop]
```

| PostgreSQL syntax | Fix |
|---|---|
| `TIMESTAMPTZ` | Use `TIMESTAMP WITH TIME ZONE` or wrap in ignore block |
| `CREATE EXTENSION` | Wrap in ignore block |
| `ON CONFLICT DO UPDATE` | Wrap in ignore block |

## Clean Architecture Placement

- Generated `Tables`, `Records`, `Keys` belong in `infrastructure` only
- Never import generated jOOQ classes from `domain` or `application`

## Runtime Configuration & Reactive Patterns

See [references/runtime.md](references/runtime.md) for:
- `JooqConfig` bean with `Settings`
- `Mono.from()` for single-row queries, `Flux.from()` for list queries
- `Mono.zip()` for running count + list queries in parallel
- `toDomain()` defined in repository's `companion object`
- `either {}` vs flat `Either.catch {}` pattern per operation type
