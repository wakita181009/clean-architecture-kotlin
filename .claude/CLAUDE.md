# Clean Architecture Kotlin Project

## Project Purpose

This is a Medium article companion repository demonstrating Clean Architecture + Kotlin + Arrow-kt.
It is intentionally structured to be readable and educational, not just functional.

## Project Overview

Spring Boot application structured in Clean Architecture with 5 Gradle modules and **CQRS** (Command Query Responsibility Segregation):

| Module           | Package            | Role                                                                |
|------------------|--------------------|---------------------------------------------------------------------|
| `domain`         | `*.domain`         | Entities, Value Objects, Repository interfaces (command only), Domain errors |
| `application`    | `*.application`    | Use cases split into `command/` and `query/` sub-packages           |
| `infrastructure` | `*.infrastructure` | Repository implementations (R2DBC), query repository impls          |
| `presentation`   | `*.presentation`   | REST controllers, GraphQL DataFetchers, DTOs                        |
| `framework`      | `*.framework`      | Spring Boot entry point, DI configuration                           |

### CQRS Pattern

The application layer is split into **command** and **query** sides:

```
application/
├── command/            # Write operations (go through domain layer)
│   ├── dto/            # Input DTOs that map to domain entities
│   ├── error/          # Command-specific application errors
│   └── usecase/        # Command use cases (e.g., Save, Create, Update, Delete)
├── query/              # Read operations (bypass domain layer)
│   ├── dto/            # Query DTOs (flat data, primitives only — no domain types)
│   ├── error/          # Query-specific application errors
│   ├── repository/     # Query repository interfaces (defined HERE, not in domain)
│   └── usecase/        # Query use cases (e.g., List, FindById)
└── error/              # Shared base (ApplicationError)
```

**Key CQRS rules:**
- **Command side**: Flows through domain layer (`use case → domain repository → domain entity`). Domain repository interface lives in `domain/`.
- **Query side**: Bypasses domain layer entirely. Query repository interface lives in `application/query/repository/`. Returns flat DTOs (`XxxQueryDto`, `PageDto`) directly — no domain entities or value objects.
- **Domain repository** (`domain/repository/`): Only write methods (`save`, `delete`, etc.). Read methods are removed.
- **Query repository** (`application/query/repository/`): Read-only methods (`findById`, `list`). Returns application-level DTOs, not domain entities.

## Architecture Rules (MUST follow)

### Module dependency direction

```
framework → presentation → application → domain ← infrastructure
```

- `domain`: no Spring dependencies; depends on `arrow-fx-coroutines` and `slf4j-api`. Contains only command-side repository interfaces.
- `application`: depends only on `:domain`. No Spring annotations. No `@Component`, no `@Repository`. Contains both command and query sub-packages. Query repository interfaces are defined here (not in domain).
- `infrastructure`: implements domain repository interfaces (command) AND application query repository interfaces (query). May use Spring, R2DBC, Flyway.
- `presentation`: depends on `:application`. May use Spring MVC / DGS. No domain logic.
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

### Layer Purity (domain / application)

Do NOT add any new library dependencies to `domain` or `application` modules (`build.gradle.kts`).
These layers must remain pure. The allowed external dependencies are fixed and final:

- `arrow-core`, `arrow-fx-coroutines`
- `kotlinx-coroutines-core`
- `slf4j-api`

If a use case seems to require a new library, implement it in `infrastructure` and expose only a domain interface.
This is enforced at the import level by the `ForbiddenLayerImport` detekt rule (whitelist-based).

### Explicit violations to never introduce

- Do NOT add `@Component` or `@Service` to use case classes — they are instantiated manually in `UseCaseConfig`
- Do NOT add Spring annotations to domain or application layer classes
- Do NOT let domain layer import from infrastructure or presentation
- Do NOT let application layer import from infrastructure or presentation
- Do NOT use `throw` in `domain`, `application`, or `infrastructure` layers — every error must be expressed as `Either<XxxError, T>`. The return type is the complete error specification; throwing makes errors invisible to callers.
- Do NOT use `throw` in presentation layer business logic (controller methods, DTO mapping). Only Spring exception handler infrastructure (e.g., `ResponseStatusException`) may throw.
- Do NOT return a bare type (non-Either) from any fallible operation in domain, application, or infrastructure — if a function can fail, its return type must be `Either`

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

**Core rule**: Outside of presentation, `throw` is forbidden. Every fallible operation returns `Either<XxxError, T>`.
The return type alone must fully specify all possible errors — no caller should need to read an implementation to know what can fail.

Layer-specific rules:
- `domain`: All functions that can fail return `Either<DomainError, T>`. No exceptions.
- `application`: All use case methods return `Either<ApplicationError, T>`. No exceptions.
- `infrastructure`: Wrap all DB/external calls with `Either.catch { }` or `either { } + try/catch`. Always rethrow `CancellationException`. Never propagate raw exceptions.
- `presentation`: Call `.fold()` on `Either` results. MAY throw `ResponseStatusException` to delegate to Spring error handling — this is the only permitted use of `throw`.

Specific patterns:
- All error types must extend the layer's sealed interface (`DomainError`, `ApplicationError`)
- Use `either { }` DSL + `.bind()` for chaining operations
- Use `mapLeft {}` to convert error types at layer boundaries

### Immutability (FP principle)

- Prefer `val` over `var` everywhere. `var` is forbidden in `domain` and `application`.
- Domain entities must be `data class` with only `val` fields. Use `.copy()` for updates.
- Collections in domain/application must be immutable (`List`, `Map`, not `MutableList`).
- Do NOT mutate state inside `either { }` blocks — derive new values with `val`.

### Value objects

- Wrap primitive IDs and strings in `@JvmInline value class`
- Use a private constructor + `companion object { operator fun invoke(...) }` pattern
- For validated value objects, provide a `fun of(...)` returning `Either`

### Naming

- Domain errors: `XxxError` (sealed interface in `domain/error/`)
- Domain repository interfaces: `XxxRepository` (in `domain/repository/`)
- Command use case interfaces: `XxxSaveUseCase`, `XxxCreateUseCase`, `XxxUpdateUseCase`, `XxxDeleteUseCase` (in `application/command/usecase/`)
- Query use case interfaces: `XxxFindByIdQueryUseCase`, `XxxListQueryUseCase` (in `application/query/usecase/`)
- Use case implementations: `XxxUseCaseImpl` (in same package as interface)
- Command application errors: `XxxSaveError` (in `application/command/error/`)
- Query application errors: `XxxFindByIdQueryError`, `XxxListQueryError` (in `application/query/error/`)
- Query DTOs: `XxxQueryDto`, `PageDto` (in `application/query/dto/`)
- Query repository interfaces: `XxxQueryRepository` (in `application/query/repository/`)
- Query repository implementations: `XxxQueryRepositoryImpl` (in `infrastructure/query/repository/`)
- Domain repository implementations: `XxxRepositoryImpl` (in `infrastructure/command/repository/`)

### DI

- DI wiring is done in `framework/config/`
- No `@Autowired` — constructor injection only
- `UseCaseConfig` wires both command use cases (with domain repository) and query use cases (with query repository)

## Language

- Code, comments, variable names: English
- Git commit messages: English

## Commands

- `./gradlew detekt` — static analysis. Fails the build if `throw` is found in domain, application, or infrastructure layers (`NoThrowOutsidePresentation` rule).
- `./gradlew ktlintCheck` — lint check
- `./gradlew test` — run unit tests
- `./gradlew koverVerify` — coverage check. Fails if line coverage across `domain`, `application`, `presentation` drops below 80%.
- `./gradlew check` — run all checks (includes detekt + ktlint + tests + koverVerify)

## Specifications

- GitHub Repository API: see `.claude/specs/github-repo-api.md`
