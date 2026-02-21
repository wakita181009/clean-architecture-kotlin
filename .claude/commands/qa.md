---
name: qa
description: Run the full quality assurance pipeline for a Kotlin CA/FP project. Executes build + lint + security check in parallel, then code review and KDoc documentation after all pass.
---

# /qa — Quality Assurance Pipeline

Run a comprehensive QA pipeline with parallel execution.
Use this to re-run QA independently, or when `/impl` QA phase needs to be retried.

## Instructions

The user has run `/qa`.

### Step 1: Verify Project

Check that a `gradlew` file exists. If not: "This command requires a Gradle project with a gradlew wrapper."

Optionally read recent changes (ask user which feature was just implemented if not clear from context).

### Step 2: Parallel QA Phase

Use **TeamCreate** to run three checks simultaneously:

```
TeamCreate team: "qa-pipeline"

Spawn simultaneously:
1. builder → run ./gradlew build (tests + compilation + coverage)
2. linter  → run ./gradlew ktlintCheck detekt
3. security-checker → static security analysis
```

All three are independent and can run in parallel.

Wait for all three to complete.

### Step 3: Evaluate Parallel Results

Collect results from all three agents.

**If any critical failures**:
- Report the failures clearly
- DO NOT proceed to code review
- Ask: "Would you like help fixing [build failures / lint violations / security issues]?"

**If all pass (or only warnings)**:
- Proceed to Step 4

### Step 4: Sequential Phase — Code Review + Documentation (parallel with each other)

Use **TeamCreate** to run two agents simultaneously:

```
TeamCreate team: "qa-review-pipeline"

Spawn simultaneously:
1. code-reviewer → comprehensive CA + Arrow + FP review
2. documenter    → KDoc generation for new public APIs
```

### Step 5: Report Final Results

Present a consolidated QA report:

```
## QA Pipeline Results

### Build: [PASS / FAIL]
[test counts, coverage %]

### Lint: [PASS / FAIL]
[ktlint violations, detekt violations]

### Security: [PASS / WARNINGS / FAIL]
[issue summary]

### Code Review: [Approved / Needs Changes]
[key findings]

### Documentation: [Complete / Skipped]
[KDoc files updated]

### Overall: [READY TO MERGE / NEEDS WORK]
```

## Parallelization Diagram

```
         ┌──────────┬──────────┬──────────┐
         │  builder │  linter  │ security │  ← parallel
         └────┬─────┴────┬─────┴────┬─────┘
              └──────────┴──────────┘
                         ↓ (all pass)
              ┌────────────────────────────┐
              │code-reviewer  │ documenter │  ← parallel
              └────────────────────────────┘
                         ↓
                  [final report]
```
