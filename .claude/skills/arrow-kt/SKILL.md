---
name: arrow-kt
description: >
  Arrow-kt patterns for this Clean Architecture Kotlin project. Use when writing or reviewing
  code that involves error handling with Either, either{} DSL, bind(), mapLeft(), Either.catch(),
  ensure(), or fold(). Covers all Arrow-kt APIs used in domain, application, infrastructure,
  and presentation layers.
---

# Arrow-kt Patterns

## Imports

```kotlin
import arrow.core.Either
import arrow.core.raise.either   // for either{} DSL
import arrow.core.raise.ensure   // for ensure() inside either{}
```

## Core APIs

### `Either<E, T>` — return type for all fallible operations

```kotlin
// Domain repository interface
fun findById(id: GitHubRepoId): Either<GitHubError, GitHubRepo>

// Application use case interface
fun execute(id: Long): Either<GitHubRepoFindByIdError, GitHubRepo>
```

### `either { }` + `.bind()` — compose multiple Either operations

```kotlin
override fun execute(pageNumber: Int, pageSize: Int): Either<GitHubRepoListError, Page<GitHubRepo>> =
    either {
        val validPageNumber = PageNumber.of(pageNumber)
            .mapLeft(GitHubRepoListError::InvalidPageNumber)
            .bind()
        val validPageSize = PageSize.of(pageSize)
            .mapLeft(GitHubRepoListError::InvalidPageSize)
            .bind()
        gitHubRepoRepository.list(validPageNumber, validPageSize)
            .mapLeft(GitHubRepoListError::FetchFailed)
            .bind()
    }
```

- `.bind()` unwraps `Right`, short-circuits on `Left`
- Only valid inside `either { }` block
- Always `mapLeft` before `.bind()` to convert error types at layer boundaries

### `.mapLeft()` — convert error types at layer boundaries

```kotlin
// Lambda style
repository.findById(id).mapLeft { GitHubRepoFindByIdError.FetchFailed(it) }

// Method reference style (when error class takes the upstream error as sole arg)
.mapLeft(GitHubRepoListError::FetchFailed)
```

### `Either.catch { }` — wrap exceptions into Either

```kotlin
Either.catch {
    jpaRepository.findById(id.value)
        .orElseThrow { NoSuchElementException("Not found: ${id.value}") }
        .toDomain()
}.mapLeft { e ->
    when (e) {
        is NoSuchElementException -> GitHubError.NotFound(id.value)
        else -> GitHubError.DatabaseError("Unexpected error", e)
    }
}
```

- Use in infrastructure layer to wrap JPA/DB calls
- Always chain `.mapLeft()` immediately after to convert to domain error types

### `ensure()` — conditional validation inside `either { }`

```kotlin
fun of(value: Int): Either<PageNumberError, PageNumber> =
    either {
        ensure(value >= MIN_VALUE) { PageNumberError.BelowMinimum(value) }
        PageNumber(value)
    }
```

- First failing `ensure` short-circuits
- Only valid inside `either { }` block

### `.fold()` — handle both branches in presentation layer

```kotlin
useCase.execute(id).fold(
    ifLeft = { error -> ResponseEntity.badRequest().body(ErrorResponse(error.message)) },
    ifRight = { result -> ResponseEntity.ok(Response.fromDomain(result)) },
)
```

Preferred over `.map{}.getOrElse{}` when both branches produce equally important results (e.g., HTTP responses).

## Layer-by-Layer Reference

For per-layer patterns and complete examples, see [references/layer-patterns.md](references/layer-patterns.md).
