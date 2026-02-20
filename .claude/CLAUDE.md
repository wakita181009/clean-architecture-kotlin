# Clean Architecture Kotlin Project

## Project Purpose

This is a Medium article companion repository demonstrating Clean Architecture + Kotlin + Arrow-kt.
It is intentionally structured to be readable and educational, not just functional.

## Project Overview

Spring Boot application structured in Clean Architecture with 5 Gradle modules:

| Module           | Package            | Role                                                          |
|------------------|--------------------|---------------------------------------------------------------|
| `domain`         | `*.domain`         | Entities, Value Objects, Repository interfaces, Domain errors |
| `application`    | `*.application`    | Use case interfaces + implementations, Application errors     |
| `infrastructure` | `*.infrastructure` | Repository implementations (R2DBC), external integrations     |
| `presentation`   | `*.presentation`   | REST controllers, DTOs                                        |
| `framework`      | `*.framework`      | Spring Boot entry point, DI configuration                     |

## Architecture Rules (MUST follow)

### Module dependency direction

```
framework → presentation → application → domain ← infrastructure
```

- `domain`: no Spring dependencies; depends on `arrow-fx-coroutines` and `slf4j-api`
- `application`: depends only on `:domain`. No Spring annotations. No `@Component`, no `@Repository`.
- `infrastructure`: implements domain interfaces. May use Spring, R2DBC, Flyway.
- `presentation`: depends on `:application`. May use Spring MVC. No domain logic.
- `framework`: wires everything together. Owns `@SpringBootApplication` and `UseCaseConfig`.

### `domain/` — FORBIDDEN imports

- `org.springframework.*`
- `jakarta.*` / `javax.*`
- `*.application.*`, `*.infrastructure.*`, `*.presentation.*`, `*.framework.*`
- Any persistence or web framework

Allowed external: `arrow.core.*`, `arrow.fx.coroutines.*`, `kotlinx.coroutines.*`, `org.slf4j.*`

### `application/` — FORBIDDEN imports

- `org.springframework.*`
- `jakarta.*` / `javax.*`
- `*.infrastructure.*`, `*.presentation.*`, `*.framework.*`
- Only allowed external: `arrow.core.*`, `arrow.fx.coroutines.*`, `kotlinx.coroutines.*`, `org.slf4j.*`

### `presentation/` — FORBIDDEN imports

- `*.infrastructure.*`

> **Note**: `presentation` may import `*.domain.*` (entities, value objects, error types) directly.
> Use cases return domain objects, so presentation needs to reference them for DTO mapping.
> Domain logic must NOT be placed in the presentation layer — use domain objects only for reading/mapping.

### Explicit violations to never introduce

- Do NOT add `@Component` or `@Service` to use case classes — they are instantiated manually in `UseCaseConfig`
- Do NOT add Spring annotations to domain or application layer classes
- Do NOT let domain layer import from infrastructure or presentation
- Do NOT let application layer import from infrastructure or presentation

## Tech Stack

- **Language**: Kotlin (JVM)
- **Framework**: Spring Boot
- **Error handling**: Arrow (`Either<Error, Result>`) — use throughout all layers
- **Testing**: JUnit5 + Kotest assertions + MockK
- **Linting**: ktlint
- **Coverage**: Kover
- **DB Migrations**: Flyway

## Coding Conventions

### Error handling

- All error types must extend the layer's sealed interface (`DomainError`, `ApplicationError`)
- All repository methods return `Either<DomainError, T>` — never throw exceptions
- All use case methods return `Either<ApplicationError, T>`
- Use `either { }` DSL + `.bind()` for chaining operations
- Use `mapLeft {}` to convert error types at layer boundaries
- Use `either { }` + `try/catch` in infrastructure to wrap R2DBC/DB exceptions (rethrow `CancellationException`)

### Value objects

- Wrap primitive IDs and strings in `@JvmInline value class`
- Use a private constructor + `companion object { operator fun invoke(...) }` pattern
- For validated value objects, provide a `fun of(...)` returning `Either`

### Naming

- Domain errors: `XxxError` (sealed class in `domain/error/`)
- Application errors: `XxxUseCaseError` or `XxxError` (sealed class in `application/error/`)
- Use case interfaces: `XxxUseCase` with a single `fun execute(...)` method
- Use case implementations: `XxxUseCaseImpl` (in same package as interface)
- R2DBC entities: `XxxR2dbcEntity` (in `infrastructure/entity/`)
- Domain-to-R2DBC mapping: extension function `fun XxxDomain.toEntity()` in the same file as `XxxR2dbcEntity`
- R2DBC-to-domain mapping: member function `fun XxxR2dbcEntity.toDomain()` in the same file

### DI

- DI wiring is done in `framework/config/`
- No `@Autowired` — constructor injection only

## Language

- Code, comments, variable names: English
- Git commit messages: English

## Specifications

- GitHub Repository REST API: see `.claude/specs/github-repo-api.md`