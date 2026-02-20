# Clean Architecture with Kotlin and Arrow-kt

A production-style reference implementation of Clean Architecture in Kotlin, using Arrow-kt for type-safe error handling.

This project serves as the companion code for a Medium article series.

## Article Series

| Article | Tag | Topics |
|---------|-----|--------|
| 1. Clean Architecture + R2DBC + REST | [article-1] | Layered modules, Arrow Either, value objects, coroutines |

## Architecture

```
framework       ← Spring Boot wiring, DI config
    ↑
presentation    ← REST controllers, DTOs, request/response mapping
    ↑
application     ← Use cases, application errors (no framework deps)
    ↑
infrastructure  ← R2DBC repositories, DB entities, adapters
    ↑
domain          ← Entities, value objects, repository interfaces, errors
```

**Dependency rule**: each layer may only depend on layers below it. Domain has zero external dependencies.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.x |
| Framework | Spring Boot 4.x |
| API | Spring WebFlux (REST) |
| Database | PostgreSQL + Spring Data R2DBC |
| Error handling | Arrow-kt (`Either`, `Raise` DSL) |
| Migration | Flyway |
| Build | Gradle (multi-project, version catalog) |

## Domain Model

The domain entity is `GitHubRepo` — a locally cached snapshot of a GitHub repository.

```
GitHubRepo
├── id: GitHubRepoId          (@JvmInline value class wrapping Long)
├── owner: GitHubOwner        (@JvmInline value class wrapping String)
├── name: GitHubRepoName      (@JvmInline value class wrapping String)
├── fullName: String
├── description: String?
├── language: String?
├── stargazersCount: Int
├── forksCount: Int
├── isPrivate: Boolean
├── createdAt: OffsetDateTime
└── updatedAt: OffsetDateTime
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/github-repos` | List repos with pagination (`pageNumber`, `pageSize`) |
| `GET` | `/api/github-repos/{id}` | Find repo by ID |
| `POST` | `/api/github-repos` | Save (upsert) a repo |

## Getting Started

### Prerequisites

- JDK 21+
- Docker

### 1. Start PostgreSQL

```bash
docker compose -f docker/compose.yml up -d
```

### 2. Configure environment

```bash
cp .env.sample .env
```

### 3. Run the application

```bash
./gradlew :framework:bootRun
```

The API is available at `http://localhost:8080`.

### Example Request

```bash
curl -X POST http://localhost:8080/api/github-repos \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "owner": "arrow-kt",
    "name": "arrow",
    "fullName": "arrow-kt/arrow",
    "description": "Functional companion to Kotlin'\''s Standard Library",
    "language": "Kotlin",
    "stargazersCount": 6000,
    "forksCount": 450,
    "isPrivate": false,
    "createdAt": "2017-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }'

curl http://localhost:8080/api/github-repos
```

## Project Structure

```
.
├── domain/                   # Pure Kotlin — no framework dependencies
│   └── src/main/kotlin/
│       ├── entity/github/    # GitHubRepository
│       ├── error/            # DomainError, GitHubError, PageNumberError, PageSizeError
│       ├── repository/       # GitHubRepoRepository (interface)
│       └── valueobject/      # Page, PageNumber, PageSize, GitHubRepoId, ...
│
├── application/              # Pure Kotlin — depends only on domain
│   └── src/main/kotlin/
│       ├── error/github/     # GitHubRepoListError, FindByIdError, SaveError
│       └── usecase/github/   # List, FindById, Save (interface + impl)
│
├── infrastructure/           # Spring + R2DBC — implements domain interfaces
│   └── src/main/kotlin/
│       ├── entity/           # GitHubRepoR2dbcEntity
│       └── repository/       # GitHubRepoR2dbcRepository, GitHubRepoRepositoryImpl
│
├── presentation/             # Spring MVC — controllers and DTOs
│   └── src/main/kotlin/
│       └── rest/             # GitHubRepoController, request/response DTOs
│
└── framework/                # Spring Boot entry point and DI wiring
    └── src/main/kotlin/
        ├── Application.kt
        └── config/           # UseCaseConfig (manual DI for use cases)
```
