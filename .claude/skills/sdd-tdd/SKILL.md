---
name: sdd-tdd
description: >
  SDD+TDD workflow for this Clean Architecture Kotlin project.
  Use when implementing a new feature end-to-end: spec → tests (red) → implementation (green) → refactor.
  Covers spec-to-layer mapping, test-first patterns per layer, coroutine testing, property-based testing,
  and the red-green-refactor cycle.
---

# SDD + TDD Workflow

## The Pipeline

```
Spec (.claude/specs/)
  ↓
Layer Sketch (entity + value objects + repository interface + use cases)
  ↓
[RED] Write failing tests: domain + application
  ↓
[GREEN] Implement all 5 layers in order:
  1. domain (value objects → errors → entity → repository interface)
  2. application (use case interface → use case Impl)
  3. infrastructure (jOOQ records → repository Impl, suspend + R2DBC)
  4. presentation (DTOs → controller or DGS resolver)
  5. framework/config (UseCaseConfig wiring)
  ↓
[VERIFY] ./gradlew test
  ↓
[REFACTOR] Clean up while keeping green
```

---

## Step 1: Read the Spec

Every feature starts with a spec in `.claude/specs/`. Extract:
- Domain model fields and types
- Value object validation rules
- Use case input/output types
- Error cases (400/404/etc.)

If no spec exists, write one first using the `spec-writer` agent.

---

## Step 2: Sketch Layer Mapping (before writing any code — CQRS-aware)

| Spec element         | CA construct                                               |
|----------------------|------------------------------------------------------------|
| Domain model field   | Entity field (`val`, immutable)                            |
| ID with validation   | `@JvmInline value class` with `fun of(...)` → `Either`     |
| Non-validated string | `@JvmInline value class` with direct constructor           |
| Write operation      | Command use case in `application/command/` + domain repository |
| Read operation       | Query use case in `application/query/` + query repository  |
| Error response 400   | `XxxError.InvalidXxx` in domain or application             |
| Error response 404   | `XxxQueryError.NotFound` in query error                    |
| Pagination params    | `PageNumber`, `PageSize` value objects                     |
| Query repo interface | In `application/query/repository/` (NOT domain)            |

---

## Step 3: Write Failing Tests (RED)

Tests are written **before** implementations. Tests reference interfaces and value object constructors that don't exist yet — the compiler errors are expected.

| Layer | Test approach |
|-------|--------------|
| `domain` | No mocks. `checkAll(Arb.xxx())` for boundaries, `shouldBeRight`/`shouldBeLeft` |
| `application` | `mockk<Repository>()`, `coEvery`/`coVerify`, `runTest {}` |
| `presentation` | Mock use cases, test HTTP status codes via `response.statusCode shouldBe ...` |

For complete test skeletons and MockK/Kotest patterns: see the `tdd-kotlin` skill.

---

## Step 4: Make Tests Pass (GREEN)

Implement the minimum code to turn red tests green. Do not gold-plate.

Implementation order (critical for layer purity — CQRS-aware):
1. **domain**: value objects (`@JvmInline`) → errors (sealed) → entity (`data class`) → repository interface (write methods only)
2. **application/command**: command DTOs → command errors → command use case interface → impl
3. **application/query**: query DTOs → query errors → query repository interface → query use case interface → impl
4. **infrastructure/repository**: command repo impl (jOOQ + R2DBC)
5. **infrastructure/query**: query repo impl (jOOQ + R2DBC, maps to query DTOs)
6. **presentation**: DTOs (`XxxRequest`, `XxxResponse`) → controller or DGS resolver
7. **framework**: wire all beans in `UseCaseConfig`

---

## Step 5: Property-Based Testing (for value objects with bounds)

Use `kotest-property` for numeric or range-validated value objects:

```kotlin
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest

class PageSizeTest {
    @Test
    fun `of returns Right for all values in 1-100`() = runTest {
        checkAll(Arb.int(1..100)) { n ->
            PageSize.of(n).shouldBeRight()
        }
    }

    @Test
    fun `of returns Left for all values below 1`() = runTest {
        checkAll(Arb.int(Int.MIN_VALUE..0)) { n ->
            PageSize.of(n).shouldBeLeft()
        }
    }

    @Test
    fun `of returns Left for all values above 100`() = runTest {
        checkAll(Arb.int(101..Int.MAX_VALUE)) { n ->
            PageSize.of(n).shouldBeLeft()
        }
    }
}
```

Especially valuable for off-by-one errors in boundary conditions.

---

## Step 6: Verify Coverage

```bash
./gradlew koverVerify  # must pass 80% line coverage threshold
```

If below threshold, identify untested branches — typically `Left` paths and null-record cases.

---

## Using the Orchestrator

For a complete automated run:
1. Ensure spec exists in `.claude/specs/<feature>.md`
2. Invoke the `orchestrator` agent with the feature name
3. The orchestrator runs all phases (RED → GREEN → VERIFY+REVIEW) and returns a full report
