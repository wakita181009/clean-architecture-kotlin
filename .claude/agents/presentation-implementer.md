---
name: presentation-implementer
description: Implement the presentation layer of a Kotlin Clean Architecture project. Creates REST controllers and/or GraphQL DataFetchers using .fold() for Either handling, request/response DTOs, and HTTP status mapping. Can run in parallel with infra-implementer after app-implementer completes.
tools: Read, Write, Edit, Glob, Grep
model: sonnet
skills:
  - ca-kotlin
  - fp-kotlin
---

# Presentation Implementer Agent

You implement the presentation layer: REST controllers, GraphQL DataFetchers, request/response DTOs.

## CQRS Note

This project uses CQRS. Controllers/DataFetchers inject **both command and query use cases**:
- **Command use cases** (e.g., `XxxSaveUseCase`): for write operations (POST, PUT, DELETE)
- **Query use cases** (e.g., `XxxFindByIdQueryUseCase`, `XxxListQueryUseCase`): for read operations (GET)

## Prerequisites

Application layer (both command and query use case interfaces) must be implemented. Before starting:
1. Read existing controller/DataFetcher files to understand the routing and response patterns
2. Read existing request/response DTO patterns
3. Check `presentation/build.gradle.kts` — does this project use REST (Spring WebFlux), GraphQL (Netflix DGS), or both?
4. Read the application use case interfaces you will call (both command and query)
5. Read `.claude/specs/[feature-name].md` for API contracts

## Key Rule: `throw` is ALLOWED here

The presentation layer is the boundary. You can throw `GraphQLException` or return `ResponseEntity` with error status.
Map `Either.Left` values to appropriate HTTP responses via `.fold()`.

## Step 1: Create Request/Response DTOs

```kotlin
// Request DTO (Presentation → Application)
data class [Entity]CreateRequest(
    val [field]: [Type],
    // ... nullable optional fields with defaults
) {
    fun toDto() = [Entity]Dto(
        [field] = [field],
        // ...
    )
}

// Response DTO (Domain → Presentation)
data class [Entity]Response(
    val id: Long,
    val [field]: [Type],
    // ... unwrap value objects to primitives
) {
    companion object {
        fun fromDomain(entity: [DomainEntity]) = [Entity]Response(
            id = entity.id.value,
            [field] = entity.[field].value,
            // ...
        )
    }
}

data class ErrorResponse(val message: String)
```

## Step 2: Implement REST Controller (if applicable)

```kotlin
@RestController
@RequestMapping("/api/[resource]")
class [Entity]Controller(
    // Command use cases
    private val [saveUseCase]: [SaveUseCase],
    // Query use cases
    private val [findByIdQueryUseCase]: [FindByIdQueryUseCase],
    private val [listQueryUseCase]: [ListQueryUseCase],
) {
    private val logger = LoggerFactory.getLogger([Entity]Controller::class.java)

    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "1") pageNumber: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ResponseEntity<*> =
        [listUseCase].execute(pageNumber, pageSize).fold(
            ifLeft = { error ->
                when (error) {
                    is [ListError].InvalidPageNumber,
                    is [ListError].InvalidPageSize ->
                        ResponseEntity.badRequest().body(ErrorResponse(error.message))
                    is [ListError].FetchFailed -> {
                        logger.error("List failed: ${error.message}")
                        ResponseEntity.internalServerError().body(ErrorResponse("Internal error"))
                    }
                }
            },
            ifRight = { page ->
                ResponseEntity.ok([Entity]ListResponse(page.totalCount, page.items.map { [Entity]Response.fromDomain(it) }))
            },
        )

    @GetMapping("/{id}")
    suspend fun findById(@PathVariable id: Long): ResponseEntity<*> =
        [findByIdUseCase].execute(id).fold(
            ifLeft = { error ->
                when (error) {
                    is [FindByIdError].InvalidId ->
                        ResponseEntity.badRequest().body(ErrorResponse(error.message))
                    is [FindByIdError].NotFound ->
                        ResponseEntity.notFound().build<Nothing>()
                    is [FindByIdError].FetchFailed -> {
                        logger.error("FindById failed (id=$id): ${error.message}")
                        ResponseEntity.internalServerError().body(ErrorResponse("Internal error"))
                    }
                }
            },
            ifRight = { entity -> ResponseEntity.ok([Entity]Response.fromDomain(entity)) },
        )

    @PostMapping
    suspend fun create(@RequestBody request: [Entity]CreateRequest): ResponseEntity<*> =
        [createUseCase].execute(request.toDto()).fold(
            ifLeft = { error ->
                when (error) {
                    is [CreateError].ValidationFailed ->
                        ResponseEntity.unprocessableEntity().body(ErrorResponse(error.message))
                    is [CreateError].Conflict ->
                        ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(error.message))
                    is [CreateError].SaveFailed -> {
                        logger.error("Create failed: ${error.message}")
                        ResponseEntity.internalServerError().body(ErrorResponse("Internal error"))
                    }
                }
            },
            ifRight = { entity -> ResponseEntity.ok([Entity]Response.fromDomain(entity)) },
        )
}
```

## Step 3: Implement GraphQL DataFetcher (Netflix DGS — if applicable)

This project may use Netflix DGS for GraphQL. Use `@DgsComponent` (not `@Component`):

```kotlin
@DgsComponent
class [Entity]DataFetcher(
    private val [listUseCase]: [ListUseCase],
    private val [findByIdUseCase]: [FindByIdUseCase],
) {
    @DgsQuery(field = "[queryName]")
    suspend fun [queryName](@InputArgument id: Long): [GraphQLType]? =
        [findByIdUseCase].execute(id).fold(
            ifLeft = { error ->
                when (error) {
                    is [FindByIdError].NotFound -> null
                    else -> throw GraphQLException(error.message)
                }
            },
            ifRight = { entity -> entity.toGraphQL() },
        )

    @DgsMutation(field = "[mutationName]")
    suspend fun [mutationName](@InputArgument input: [InputType]): [GraphQLType] =
        [createUseCase].execute(input.toDto()).fold(
            ifLeft = { error -> throw GraphQLException(error.message) },
            ifRight = { entity -> entity.toGraphQL() },
        )
}

// Mapper extension functions (in separate file: [Entity]GraphQLMapper.kt)
fun [DomainEntity].toGraphQL() = [GraphQLType](
    id = id.value,
    // ...
)
```

## Step 4: Update GraphQL Schema (if applicable)

Add to `src/main/resources/schema/[domain].graphqls`:
```graphql
type Query {
    [queryName]([args]): [Type]
}

type Mutation {
    [mutationName](input: [InputType]!): [Type]!
}

type [Type] {
    id: Long!
    [field]: String!
}
```

## Step 5: Implement Presentation Unit Tests

Write complete unit tests (not skeletons):
- Mock all use cases with `mockk<T>()`
- Test each `ResponseEntity` status code
- Test error response body shape
- Test success response body shape
- Use `runTest { }` and `coEvery` for suspend functions

## Output

Report every file created. Note any HTTP status decisions made.
