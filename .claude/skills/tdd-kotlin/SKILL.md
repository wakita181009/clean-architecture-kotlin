---
name: tdd-kotlin
description: >
  TDD patterns for Kotlin CA/FP projects using Kotest + MockK + Arrow-kt. Use when writing
  tests, designing test cases, or following the red-green-refactor cycle. Triggers on: test
  file creation, writing value object tests, use case tests with mocked repositories,
  controller tests, property-based testing, coEvery/coVerify patterns.
---

# TDD for Kotlin CA/FP

## Test Stack

| Library | Purpose |
|---------|---------|
| `kotlin("test")` | `@Test` annotation |
| `kotest-assertions-core` | `shouldBe`, `shouldBeInstanceOf` |
| `kotest-assertions-arrow` | `shouldBeRight()`, `shouldBeLeft()` |
| `kotest-property` | `checkAll(Arb.xxx()) { }` |
| `mockk` | `mockk<T>()`, `coEvery`, `coVerify` |
| `kotlinx-coroutines-test` | `runTest { }` |

## Layer Test Patterns

| Layer | Approach |
|-------|---------|
| Domain | No mocks. `checkAll` for boundaries, `shouldBeRight`/`shouldBeLeft` for Either results |
| Application | `mockk<Repository>()`, `coEvery`/`coVerify`, `runTest {}` |
| Presentation | `mockk<UseCase>()`, test each HTTP status code via `response.statusCode shouldBe ...` |
| Infrastructure | Integration tests only — requires test containers or H2 |

## Key Assertions

```kotlin
result.shouldBeRight(expectedValue)           // assert Right with specific value
result.shouldBeLeft().shouldBeInstanceOf<E>() // assert Left type
error.message shouldContain "expected text"   // assert error message content

coVerify(exactly = 1) { repo.save(any()) }   // verify call count
coVerify(exactly = 0) { repo.save(any()) }   // verify NOT called
```

## Test Naming

```kotlin
@Test
fun `of returns Left InvalidId when value is zero or negative`() = runTest { ... }

@Test
fun `execute returns Left FetchFailed when repository throws exception`() = runTest { ... }
```

For complete test skeletons per layer (domain/application/presentation): see [references/test-patterns.md](references/test-patterns.md).
