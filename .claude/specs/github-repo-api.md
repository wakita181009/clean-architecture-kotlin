# GitHub Repository REST API Specification

## Overview

REST API for managing GitHub repository records stored in the local database.
These records represent GitHub repository metadata that has been saved via the POST endpoint.

Base path: `/api/github-repos`

---

## Domain Model

### GitHubRepository (domain entity)

| Field             | Type             | Nullable | Description                                    |
|-------------------|------------------|----------|------------------------------------------------|
| `id`              | `Long`           | No       | GitHub repository ID (numeric)                 |
| `owner`           | `String`         | No       | Repository owner (GitHub username or org name) |
| `name`            | `String`         | No       | Repository name                                |
| `fullName`        | `String`         | No       | Full name in `owner/name` format               |
| `description`     | `String`         | Yes      | Repository description                         |
| `language`        | `String`         | Yes      | Primary programming language                   |
| `stargazersCount` | `Int`            | No       | Number of stars                                |
| `forksCount`      | `Int`            | No       | Number of forks                                |
| `isPrivate`       | `Boolean`        | No       | Whether the repository is private              |
| `createdAt`       | `OffsetDateTime` | No       | Repository creation timestamp (ISO 8601)       |
| `updatedAt`       | `OffsetDateTime` | No       | Repository last updated timestamp (ISO 8601)   |

### Value Objects

| Value Object     | Backing type | Validation                                                  |
|------------------|--------------|-------------------------------------------------------------|
| `GitHubRepoId`   | `Long`       | Must be parseable as `Long`; error: `GitHubError.InvalidId` |
| `GitHubOwner`    | `String`     | No validation                                               |
| `GitHubRepoName` | `String`     | No validation                                               |

---

## Endpoints

### GET /api/github-repos

Returns a paginated list of saved GitHub repositories.

#### Query Parameters

| Parameter    | Type  | Default | Constraints | Description              |
|--------------|-------|---------|-------------|--------------------------|
| `pageNumber` | `Int` | `1`     | `>= 1`      | Page number (1-based)    |
| `pageSize`   | `Int` | `20`    | `1–100`     | Number of items per page |

#### Response: 200 OK

```json
{
  "items": [
    {
      "id": 123456,
      "owner": "octocat",
      "name": "hello-world",
      "fullName": "octocat/hello-world",
      "description": "My first repository on GitHub!",
      "language": "Kotlin",
      "stargazersCount": 1234,
      "forksCount": 56,
      "isPrivate": false,
      "createdAt": "2024-01-01T00:00:00+09:00",
      "updatedAt": "2024-06-01T12:00:00+09:00"
    }
  ],
  "totalCount": 42
}
```

#### Response: 400 Bad Request

Returned when `pageNumber < 1` or `pageSize` is out of range.

```json
{
  "message": "<error description>"
}
```

---

### GET /api/github-repos/{id}

Returns a single GitHub repository by its ID.

#### Path Parameters

| Parameter | Type   | Description          |
|-----------|--------|----------------------|
| `id`      | `Long` | GitHub repository ID |

#### Response: 200 OK

```json
{
  "id": 123456,
  "owner": "octocat",
  "name": "hello-world",
  "fullName": "octocat/hello-world",
  "description": "My first repository on GitHub!",
  "language": "Kotlin",
  "stargazersCount": 1234,
  "forksCount": 56,
  "isPrivate": false,
  "createdAt": "2024-01-01T00:00:00+09:00",
  "updatedAt": "2024-06-01T12:00:00+09:00"
}
```

#### Response: 404 Not Found

Returned when no record with the given `id` exists.

#### Response: 400 Bad Request

Returned when `id` cannot be parsed as `Long`.

```json
{
  "message": "Invalid GitHub repo ID format"
}
```

---

### POST /api/github-repos

Saves a GitHub repository record to the database.

#### Request Body

```json
{
  "id": 123456,
  "owner": "octocat",
  "name": "hello-world",
  "fullName": "octocat/hello-world",
  "description": "My first repository on GitHub!",
  "language": "Kotlin",
  "stargazersCount": 1234,
  "forksCount": 56,
  "isPrivate": false,
  "createdAt": "2024-01-01T00:00:00+09:00",
  "updatedAt": "2024-06-01T12:00:00+09:00"
}
```

| Field             | Type             | Required | Default |
|-------------------|------------------|----------|---------|
| `id`              | `Long`           | Yes      | —       |
| `owner`           | `String`         | Yes      | —       |
| `name`            | `String`         | Yes      | —       |
| `fullName`        | `String`         | Yes      | —       |
| `description`     | `String`         | No       | `null`  |
| `language`        | `String`         | No       | `null`  |
| `stargazersCount` | `Int`            | No       | `0`     |
| `forksCount`      | `Int`            | No       | `0`     |
| `isPrivate`       | `Boolean`        | No       | `false` |
| `createdAt`       | `OffsetDateTime` | Yes      | —       |
| `updatedAt`       | `OffsetDateTime` | Yes      | —       |

#### Response: 200 OK

Returns the saved repository (same schema as `GET /api/github-repos/{id}`).

#### Response: 400 Bad Request

Returned when the record cannot be saved (e.g., database error).

```json
{
  "message": "<error description>"
}
```

---

## Error Response Schema

All error responses share the following structure:

```json
{
  "message": "<human-readable error description>"
}
```

---

## Layer Mapping (CQRS)

### Command Side (Write)

| Layer              | Key classes                                                                  |
|--------------------|------------------------------------------------------------------------------|
| Domain entity      | `GitHubRepo`                                                                 |
| Domain errors      | `GitHubError` (`InvalidId`, `NotFound`, `RepositoryError`)                   |
| Domain repo iface  | `GitHubRepoRepository` (in `domain/`) — `save()` only                        |
| Command use case   | `GitHubRepoSaveUseCase` (in `application/command/usecase/`)                  |
| Command DTO        | `GitHubRepoDto` (in `application/command/dto/`)                              |
| Command errors     | `GitHubRepoSaveError` (in `application/command/error/`)                      |
| Infra repo impl    | `GitHubRepoRepositoryImpl` (jOOQ + R2DBC, in `infrastructure/command/repository/`)   |

### Query Side (Read)

| Layer              | Key classes                                                                  |
|--------------------|------------------------------------------------------------------------------|
| Query repo iface   | `GitHubRepoQueryRepository` (in `application/query/repository/`)             |
| Query use cases    | `GitHubRepoFindByIdQueryUseCase`, `GitHubRepoListQueryUseCase` (in `application/query/usecase/`) |
| Query DTOs         | `GitHubRepoQueryDto`, `PageDto` (in `application/query/dto/`)               |
| Query errors       | `GitHubRepoFindByIdQueryError`, `GitHubRepoListQueryError` (in `application/query/error/`) |
| Infra query impl   | `GitHubRepoQueryRepositoryImpl` (jOOQ + R2DBC, in `infrastructure/query/repository/`)   |

### Presentation (shared)

| Layer              | Key classes                                                                  |
|--------------------|------------------------------------------------------------------------------|
| REST Controller    | `GitHubRepoController`                                                       |
| GraphQL            | `GitHubRepoDataFetcher`, `GitHubRepoGraphQLMapper`                           |
| Request DTO        | `GitHubRepoRequest`                                                          |
| Response DTOs      | `GitHubRepoResponse`, `GitHubRepoListResponse`                               |
