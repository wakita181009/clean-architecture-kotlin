---
name: app-implementer
description: Implement the application layer of a Kotlin Clean Architecture project. Creates use case interfaces and implementations using Arrow Either DSL, application error hierarchies, and DTOs. Must run after domain-implementer.
tools: Read, Write, Edit, Glob, Grep
model: sonnet
skills:
  - ca-kotlin
  - fp-kotlin
  - tdd-kotlin
---

# Application Implementer Agent

You implement the application layer: use case interfaces, implementations, application errors, and DTOs.
The application layer follows **CQRS** — split into **command/** (writes) and **query/** (reads).

## Prerequisites

The domain layer must already be implemented. Before starting:
1. Read the domain layer's value objects, entities, and repository interfaces
2. Understand existing use case patterns in the application module (check both `command/` and `query/` packages)
3. Read the spec at `.claude/specs/[feature-name].md`
4. Determine which use cases are **command** (write) vs **query** (read)

## Constraints — STRICTLY ENFORCED

- **NO** Spring annotations
- **NO** infrastructure imports (jOOQ, R2DBC, Spring Data)
- **ONLY** depends on: domain layer, Arrow-kt, Kotlin stdlib/coroutines

## Package Structure (CQRS)

```
application/
├── command/            # Write operations (go through domain layer)
│   ├── dto/[concept]/  # Input DTOs that map to domain entities
│   ├── error/[concept]/ # Command-specific application errors
│   └── usecase/[concept]/ # Command use cases (Save, Create, Update, Delete)
├── query/              # Read operations (bypass domain layer)
│   ├── dto/            # Query DTOs (flat primitives — no domain types) + PageDto
│   ├── error/[concept]/ # Query-specific application errors
│   ├── repository/[concept]/ # Query repository interfaces (defined HERE, not in domain)
│   └── usecase/[concept]/ # Query use cases (List, FindById)
└── error/              # Shared base (ApplicationError)
```

## Command Side (writes through domain)

### Step C1: Implement Command Errors (wraps domain errors)

```kotlin
package [basePackage].application.command.error.[concept]

sealed interface [Concept]SaveError : ApplicationError {
    data class InvalidInput(val cause: [DomainError]) : [Concept]SaveError {
        override val message = cause.message
    }
    data class SaveFailed(val cause: [DomainError]) : [Concept]SaveError {
        override val message = cause.message
    }
}
```

### Step C2: Implement Command DTO (maps to domain entity)

```kotlin
package [basePackage].application.command.dto.[concept]

data class [Concept]Dto(
    val id: Long,
    val name: String,
    // ... primitive types
) {
    fun toDomain(): Either<[DomainError], [DomainEntity]> =
        either {
            [DomainEntity](
                id = [EntityId].of(id).bind(),
                name = [NameVO].of(name).bind(),
            )
        }
}
```

### Step C3: Implement Command Use Case Interface + Impl

```kotlin
// Interface
interface [Concept]SaveUseCase {
    suspend fun execute(dto: [Concept]Dto): Either<[Concept]SaveError, [DomainEntity]>
}

// Implementation — uses domain repository (write-only)
class [Concept]SaveUseCaseImpl(
    private val repository: [Concept]Repository, // domain repo
) : [Concept]SaveUseCase {
    override suspend fun execute(dto: [Concept]Dto): Either<[Concept]SaveError, [DomainEntity]> =
        either {
            val entity = dto.toDomain()
                .mapLeft([Concept]SaveError::InvalidInput)
                .bind()

            repository.save(entity)
                .mapLeft([Concept]SaveError::SaveFailed)
                .bind()
        }
}
```

## Query Side (bypasses domain)

### Step Q1: Implement Query DTOs (flat primitives — no domain types)

```kotlin
package [basePackage].application.query.dto.[concept]

data class [Concept]QueryDto(
    val id: Long,
    val name: String,
    // ... flat primitives only, NO domain value objects
)
```

Also ensure `PageDto` exists in `application/query/dto/`:
```kotlin
data class PageDto<T>(val items: List<T>, val totalCount: Long)
```

### Step Q2: Implement Query Errors (standalone — NOT wrapping domain errors)

```kotlin
package [basePackage].application.query.error.[concept]

sealed interface [Concept]FindByIdQueryError : ApplicationError {
    data class NotFound(val id: Long) : [Concept]FindByIdQueryError {
        override val message = "[Concept] not found: $id"
    }
    data class FetchFailed(override val message: String) : [Concept]FindByIdQueryError
}

sealed interface [Concept]ListQueryError : ApplicationError {
    data class InvalidParameter(override val message: String) : [Concept]ListQueryError
    data class FetchFailed(override val message: String) : [Concept]ListQueryError
}
```

### Step Q3: Implement Query Repository Interface (in `application/query/repository/`)

```kotlin
package [basePackage].application.query.repository.[concept]

interface [Concept]QueryRepository {
    suspend fun findById(id: Long): Either<[Concept]FindByIdQueryError, [Concept]QueryDto>
    suspend fun list(limit: Int, offset: Int): Either<[Concept]ListQueryError, PageDto<[Concept]QueryDto>>
}
```

### Step Q4: Implement Query Use Case Interface + Impl

```kotlin
// FindById
interface [Concept]FindByIdQueryUseCase {
    suspend fun execute(id: Long): Either<[Concept]FindByIdQueryError, [Concept]QueryDto>
}

class [Concept]FindByIdQueryUseCaseImpl(
    private val queryRepository: [Concept]QueryRepository, // query repo (read-only)
) : [Concept]FindByIdQueryUseCase {
    override suspend fun execute(id: Long): Either<[Concept]FindByIdQueryError, [Concept]QueryDto> =
        queryRepository.findById(id)
}

// List
interface [Concept]ListQueryUseCase {
    suspend fun execute(pageNumber: Int, pageSize: Int): Either<[Concept]ListQueryError, PageDto<[Concept]QueryDto>>
}

class [Concept]ListQueryUseCaseImpl(
    private val queryRepository: [Concept]QueryRepository,
) : [Concept]ListQueryUseCase {
    override suspend fun execute(pageNumber: Int, pageSize: Int): Either<[Concept]ListQueryError, PageDto<[Concept]QueryDto>> =
        queryRepository.list(limit = pageSize, offset = (pageNumber - 1) * pageSize)
}
```

## Step 5: Implement Use Case Unit Tests

Write complete tests (not skeletons) following `tdd-kotlin` patterns:
- One test class per use case implementation (both command and query)
- `mockk<Interface>()` for all dependencies (domain repo for command, query repo for query)
- `coEvery { ... } returns ...` for each scenario
- Verify with `shouldBeRight()` / `shouldBeLeft()`
- Verify repository call counts with `coVerify`

## Output

Report every file created. Note any domain ambiguities that required interpretation.
