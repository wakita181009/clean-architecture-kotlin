# Feature Spec: [Feature Name]

**Date**: [YYYY-MM-DD]
**Domain**: [Domain / Bounded Context]
**Author**: [Name]
**Status**: Draft | Review | Approved

---

## 1. Context

[Why this feature exists. What business problem it solves. Domain background and motivation.]

---

## 2. Ubiquitous Language

| Term | Definition |
|------|-----------|
| [Term] | [Domain definition — precise meaning in this bounded context] |

---

## 3. Domain Model Changes

### New Entities

- `[EntityName]`: [What it represents]
  - Identity: `[IdType]` (Long / UUID / String)
  - Key attributes: [list]
  - Invariants: [business rules that must always hold]

### New / Modified Value Objects

- `[ValueObjectName]` (`@JvmInline value class`): [What it represents]
  - Valid when: [conditions]
  - Invalid when: [conditions → `[ErrorType]`]
  - Error message: "[exact message format]"

### Domain Errors

`sealed interface [FeatureName]Error : DomainError`:

| Variant | Type | When | Message |
|---------|------|------|---------|
| `[ErrorName]` | `data class` / `data object` | [When it occurs] | "[message format]" |

### Domain Repository Interface Changes (Command — write only)

Add to `[RepositoryInterfaceName]` (in `domain/repository/`):

```kotlin
suspend fun save([param]: [Entity]): Either<[ErrorType], [Entity]>
```

### Query Repository Interface (Read — in application layer)

New interface in `application/query/repository/`:

```kotlin
interface [Concept]QueryRepository {
    suspend fun findById(id: Long): Either<[FindByIdQueryError], [Concept]QueryDto>
    suspend fun list(limit: Int, offset: Int): Either<[ListQueryError], PageDto<[Concept]QueryDto>>
}
```

---

## 4. Use Cases (CQRS)

### Command UC-1: [Write Use Case Name]

**Side**: Command (write)
**Actor**: [Who triggers this — user, system, scheduler]
**Trigger**: [What triggers it — API call, event, schedule]

**Input**:

| Parameter | Primitive Type | Constraints |
|-----------|---------------|-------------|
| [param] | [Int/String/Long/...] | [validation rules] |

**Happy Path**:
1. Validate [param] → [ValueObject]
2. [Business logic step]
3. Persist via domain `[RepositoryInterface]`
4. Returns: domain entity

**Error Cases**:

| Error | `ApplicationError` Type | When |
|-------|------------------------|------|
| [description] | `[CommandUseCaseName]Error.[Variant]` | [condition] |

**Application Error Type** (wraps domain errors):

`sealed interface [CommandUseCaseName]Error : ApplicationError`:
- `[Variant](val cause: [DomainError])`: wraps [DomainErrorType] when [condition]

### Query UC-2: [Read Use Case Name]

**Side**: Query (read — bypasses domain)
**Actor**: [Who triggers this]
**Trigger**: [What triggers it]

**Input**:

| Parameter | Primitive Type | Constraints |
|-----------|---------------|-------------|
| [param] | [Int/Long/...] | [validation rules] |

**Happy Path**:
1. Validate [param] → [ValueObject] (if needed)
2. Query via `[QueryRepository]` (in `application/query/repository/`)
3. Returns: `[QueryDto]` or `PageDto<[QueryDto]>` (flat primitives)

**Error Cases**:

| Error | `ApplicationError` Type | When |
|-------|------------------------|------|
| [description] | `[QueryUseCaseName]Error.[Variant]` | [condition] |

**Application Error Type** (standalone — not wrapping domain errors):

`sealed interface [QueryUseCaseName]Error : ApplicationError`:
- `NotFound(val id: Long)`: when record not found
- `FetchFailed(override val message: String)`: when query fails

---

## 5. Infrastructure Changes

### Database

- New table: `[table_name]`
  ```sql
  CREATE TABLE [table_name] (
      [column] [TYPE] [constraints],
      ...
  );
  ```
- Migration file: `V[version]__[description].sql`

### Repository Implementation Notes

[Any notes about query complexity, transactions, reactive patterns needed]

---

## 6. Presentation Changes

### REST API

| Method | Path | Request Body | Response Body | Error HTTP Codes |
|--------|------|-------------|---------------|-----------------|
| [METHOD] | `/api/[path]` | `[RequestDto]` | `[ResponseDto]` | 400, 404, 422, 500 |

### REST Response Mapping

| `ApplicationError` Variant | HTTP Status |
|---------------------------|-------------|
| `[ValidationError]` | 400 Bad Request |
| `[NotFoundError]` | 404 Not Found |
| `[DomainValidationError]` | 422 Unprocessable Entity |
| `[InfraError]` | 500 Internal Server Error |

### GraphQL (if applicable)

```graphql
# New types / queries / mutations
type Query {
    [queryName]([args]): [Type]
}
```

---

## 7. Acceptance Criteria

- [ ] AC-1: Given [condition], when [action], then [expected result]
- [ ] AC-2: Given [invalid input], when [action], then [error response with status and message]
- [ ] AC-3: Given [not found case], when [action], then 404 response
- [ ] AC-4: [Coverage: all error paths are covered by tests]
