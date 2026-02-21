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
// Domain repository interface (CQRS: write-only)
fun save(entity: GitHubRepo): Either<GitHubError, GitHubRepo>

// Application command use case
fun execute(dto: GitHubRepoDto): Either<GitHubRepoSaveError, GitHubRepo>

// Application query use case (bypasses domain)
fun execute(id: Long): Either<GitHubRepoFindByIdQueryError, GitHubRepoQueryDto>
```

### `either { }` + `.bind()` — compose multiple Either operations

```kotlin
// Command use case example
override fun execute(dto: GitHubRepoDto): Either<GitHubRepoSaveError, GitHubRepo> =
    either {
        val entity = dto.toDomain()
            .mapLeft(GitHubRepoSaveError::InvalidInput)
            .bind()
        gitHubRepoRepository.save(entity)    // domain repo (write-only)
            .mapLeft(GitHubRepoSaveError::SaveFailed)
            .bind()
    }

// Query use case example (bypasses domain, uses query repository)
override fun execute(pageNumber: Int, pageSize: Int): Either<GitHubRepoListQueryError, PageDto<GitHubRepoQueryDto>> =
    gitHubRepoQueryRepository.list(             // query repo (read-only)
        limit = pageSize,
        offset = (pageNumber - 1) * pageSize,
    )
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

For per-layer CA patterns and complete implementation examples (domain/application/infrastructure/presentation, including jOOQ + DGS specifics): see the `ca-kotlin` skill.
