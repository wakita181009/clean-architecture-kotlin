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
| Language | Kotlin 2.3.10 (JVM 25) |
| Framework | Spring Boot 4.0.3 |
| API | Spring WebFlux (REST, reactive) |
| Database | PostgreSQL 18 + Spring Data R2DBC |
| Error handling | Arrow-kt 2.2.1.1 (`Either`, `either {}` DSL) |
| Migration | Flyway |
| Build | Gradle (multi-project, version catalog) |
| Testing | JUnit5 + Kotest 6.1.3 + MockK 1.14.9 |
| Coverage | Kover 0.9.7 |
| Linting | ktlint 14.0.1 |

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

- JDK 25+
- Docker

### 1. Start PostgreSQL

```bash
docker compose -f docker/compose.yml up -d
```

PostgreSQL listens on `localhost:6686`.

### 2. Configure environment

```bash
cp .env.sample .env
```

### 3. Run the application

```bash
./gradlew :framework:bootRun
```

The API is available at `http://localhost:8080`.

### Example Requests

```bash
# Save a repo
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

# List repos
curl "http://localhost:8080/api/github-repos?pageNumber=1&pageSize=10"

# Find by ID
curl http://localhost:8080/api/github-repos/1
```

## Project Structure

```
.
├── domain/                   # Pure Kotlin — no framework dependencies
│   └── src/main/kotlin/
│       ├── entity/github/    # GitHubRepo
│       ├── error/            # DomainError, GitHubError, PageNumberError, PageSizeError
│       ├── repository/       # GitHubRepoRepository (interface)
│       └── valueobject/      # Page, PageNumber, PageSize, GitHubRepoId, GitHubOwner, GitHubRepoName
│
├── application/              # Pure Kotlin — depends only on domain
│   └── src/main/kotlin/
│       ├── dto/github/       # GitHubRepoDto
│       ├── error/github/     # GitHubRepoListError, GitHubRepoFindByIdError, GitHubRepoSaveError
│       └── usecase/github/   # GitHubRepoListUseCase, GitHubRepoFindByIdUseCase, GitHubRepoSaveUseCase (interface + impl)
│
├── infrastructure/           # Spring + R2DBC — implements domain interfaces
│   └── src/main/kotlin/
│       ├── entity/           # GitHubRepoR2dbcEntity
│       └── repository/       # GitHubRepoR2dbcRepository, GitHubRepoRepositoryImpl
│
├── presentation/             # Spring WebFlux — controllers and DTOs
│   └── src/main/kotlin/
│       └── rest/             # GitHubRepoController
│           └── dto/          # GitHubRepoRequest, GitHubRepoResponse, GitHubRepoListResponse, ErrorResponse
│
└── framework/                # Spring Boot entry point and DI wiring
    └── src/main/kotlin/
        ├── Application.kt
        └── config/           # UseCaseConfig (manual DI for use cases)
```

## Running Tests

```bash
./gradlew test
```

## Code Coverage

Coverage is collected via Kover across the `domain`, `application`, and `presentation` modules.

```bash
./gradlew koverHtmlReport
```

## Linting

```bash
./gradlew ktlintCheck
./gradlew ktlintFormat
```

## Docker

A `Dockerfile` is provided for containerized deployment. Build the fat JAR first:

```bash
./gradlew :framework:bootJar
docker build -t clean-architecture-kotlin .
```
