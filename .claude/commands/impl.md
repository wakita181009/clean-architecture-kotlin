---
name: impl
description: Orchestrate Clean Architecture implementation of a Kotlin feature using parallel agents. Implements domain → application (sequential), then infra + presentation + tests (parallel), then runs QA pipeline.
argument-hint: [feature-name]
---

# /impl — Implementation Orchestrator

Implement a feature following Clean Architecture layer order with parallel execution where possible.
After implementation, automatically runs the QA pipeline (build + lint + security + code-review).

## Instructions

The user has run `/impl $ARGUMENTS`.

### Step 1: Verify Prerequisites

1. Check `.claude/specs/$ARGUMENTS.md` exists. If not: "Run `/spec $ARGUMENTS` first."
2. Read the spec to understand the full scope of changes.
3. Read `settings.gradle.kts` to identify module names.
4. Read the domain module to understand existing package structure.

### Step 2: Discover Project Tech Stack

Read the relevant `build.gradle.kts` files to detect:
- Does the project use REST (Spring WebFlux)?
- Does the project use GraphQL (Netflix DGS)?
- Does the project use jOOQ? R2DBC? JDBC?
- What test libraries are configured?

### Step 3: Domain Layer (Sequential — must complete first)

Use the Task tool to delegate to the `domain-implementer` agent:
- Pass the spec contents from `.claude/specs/$ARGUMENTS.md`
- Pass the discovered base package and existing domain patterns
- **CQRS**: Domain repository interface must contain **write methods only** (`save`, `delete`). Read methods go in query repository (application layer).
- Wait for completion before proceeding

**Why sequential**: Application layer depends on the domain's value objects, errors, and repository interfaces.

### Step 4: Application Layer (Sequential — after domain, CQRS)

Use the Task tool to delegate to the `app-implementer` agent:
- Pass the spec contents
- Pass the domain layer changes just implemented
- **CQRS**: The agent must implement both sides:
  - **Command side** (`application/command/`): command DTOs, command errors (wraps domain errors), command use cases (uses domain repository)
  - **Query side** (`application/query/`): query DTOs (flat primitives), query errors (standalone), query repository interface (in `application/query/repository/`), query use cases (uses query repository)
- Wait for completion before proceeding

**Why sequential**: Infrastructure and presentation both depend on application use case interfaces (both command and query).

### Step 5: Parallel Phase — Infrastructure + Presentation + Tests

Now that domain and application are complete, use **TeamCreate** to run three agents in parallel:

```
TeamCreate team: "impl-[feature-name]-parallel"

Spawn simultaneously:
1. infra-implementer → implement infrastructure layer (CQRS: both command repo in infrastructure/command/repository/ and query repo in infrastructure/query/repository/)
2. presentation-implementer → implement presentation layer (injects both command and query use cases)
3. test-implementer → implement tests for domain + application layers (both command and query use cases)
```

All three agents can work independently because:
- `infra-implementer` only needs the domain repository interface (command) + application query repository interface
- `presentation-implementer` only needs the application use case interfaces (both command and query)
- `test-implementer` tests domain/application code that is already written

Wait for all three to complete.

### Step 6: QA Pipeline (after implementation completes)

Use **TeamCreate** to run QA checks in parallel:

```
TeamCreate team: "qa-[feature-name]-pipeline"

Spawn simultaneously:
1. builder → run ./gradlew build
2. linter → run ./gradlew ktlintCheck detekt
3. security-checker → static security analysis
```

Wait for all three to complete.

**If any critical failures**: Report the failures and stop. Do NOT proceed to code review.
Ask: "Would you like help fixing [build failures / lint violations / security issues]?"

**If all pass**: Proceed to code review.

### Step 7: Code Review (Sequential — after all checks pass)

Use the Task tool to delegate to the `code-reviewer` agent:
- Pass the summary of what was implemented
- Pass any lint warnings for context
- Wait for the full review

Then delegate to the `documenter` agent for KDoc generation on public APIs.

### Step 8: Report Results

Summarize implementation and QA results:

```
## /impl $ARGUMENTS — Complete

### Implementation
- Domain: [files created]
- Application: [files created]
- Infrastructure: [files created]
- Presentation: [files created]
- Tests: [test count per layer]

### QA Pipeline
- Build: [PASS / FAIL]
- Lint: [PASS / FAIL]
- Security: [PASS / WARNINGS]
- Code Review: [Approved / Needs Changes]

### Overall: [READY TO MERGE / NEEDS WORK]
```

## Parallelization Diagram

```
[spec read] → [domain-implementer] → [app-implementer]
                                              ↓ (all 3 parallel)
                              ┌───────────────┬───────────────┐
                              │infra-impl     │pres-impl      │test-impl│
                              └───────────────┴───────────────┘
                                              ↓ (all 3 parallel)
                              ┌───────────────┬───────────────┐
                              │   builder     │   linter      │security │
                              └───────────────┴───────────────┘
                                              ↓ (sequential)
                                       [code-reviewer]
                                       [documenter]
```
