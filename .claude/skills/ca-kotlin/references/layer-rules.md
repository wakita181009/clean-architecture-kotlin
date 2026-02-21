# Clean Architecture Layer Rules for Kotlin

## Layer 1: Domain (Innermost)

**Purpose**: Business logic and rules. The heart of the application.

**Allowed dependencies**:
- Kotlin stdlib
- Arrow-kt (`arrow-core`, `arrow-fx-coroutines`)
- SLF4J (logging interface only)
- Own package

**Forbidden dependencies**:
- Spring (no `@Component`, `@Repository`, `@Service`, `@Autowired`)
- JooQ, R2DBC, JDBC
- Any infrastructure framework

**What belongs here**:

### Entities
```kotlin
// Identified by ID, can change state over time
data class Order(
    val id: OrderId,
    val customerId: CustomerId,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val createdAt: OffsetDateTime,
)

enum class OrderStatus { PENDING, CONFIRMED, CANCELLED }
```

### Value Objects
```kotlin
// Identified by value, immutable, always valid after construction
@JvmInline
value class OrderId private constructor(val value: Long) {
    companion object {
        // Direct construction (trust internal use)
        operator fun invoke(value: Long) = OrderId(value)

        // Validated construction (for external input)
        fun of(value: Long): Either<OrderError, OrderId> =
            either {
                ensure(value > 0L) { OrderError.InvalidId(value) }
                OrderId(value)
            }
    }
}
```

### Domain Errors
```kotlin
interface DomainError {
    val message: String
}

sealed interface OrderError : DomainError {
    data class InvalidId(val value: Long) : OrderError {
        override val message = "Invalid order ID: $value (must be positive)"
    }
    data class NotFound(val id: OrderId) : OrderError {
        override val message = "Order not found: ${id.value}"
    }
    data object AlreadyCancelled : OrderError {
        override val message = "Order is already cancelled"
    }
    data class RepositoryError(
        override val message: String,
        val cause: Throwable? = null,
    ) : OrderError
}
```

### Repository Interfaces (Command Only)
```kotlin
// Interface defined in domain, implemented in infrastructure
// CQRS: Only write operations — read operations are in application/query/repository/
interface OrderRepository {
    suspend fun save(order: Order): Either<OrderError, Order>
    suspend fun delete(id: OrderId): Either<OrderError, Unit>
}
```

---

## Layer 2: Application (CQRS)

**Purpose**: Orchestrates domain objects to fulfill use cases. Split into **command** (writes) and **query** (reads).

**Allowed dependencies**:
- Domain layer (`project(":domain")`)
- Arrow-kt
- Kotlin stdlib / coroutines

**Forbidden dependencies**:
- Spring (no framework annotations)
- Infrastructure classes (JooQ, R2DBC)
- Presentation classes

**Package structure**:
```
application/
├── command/            # Write operations (go through domain layer)
│   ├── dto/            # Input DTOs that map to domain entities
│   ├── error/          # Command-specific application errors
│   └── usecase/        # Command use cases (Save, Create, Update, Delete)
├── query/              # Read operations (bypass domain layer)
│   ├── dto/            # Query DTOs (flat primitives — no domain types)
│   ├── error/          # Query-specific application errors
│   ├── repository/     # Query repository interfaces (defined HERE, not in domain)
│   └── usecase/        # Query use cases (List, FindById)
└── error/              # Shared base (ApplicationError)
```

### Command Side (writes through domain)

#### Command Use Case Interface
```kotlin
interface OrderSaveUseCase {
    // Input: DTO with primitive types
    suspend fun execute(dto: OrderDto): Either<OrderSaveError, Order>
}
```

#### Command Use Case Implementation
```kotlin
class OrderSaveUseCaseImpl(
    private val orderRepository: OrderRepository, // domain repository (write-only)
) : OrderSaveUseCase {
    override suspend fun execute(dto: OrderDto): Either<OrderSaveError, Order> =
        either {
            val order = dto.toDomain()
                .mapLeft(OrderSaveError::InvalidInput)
                .bind()

            orderRepository.save(order)
                .mapLeft(OrderSaveError::SaveFailed)
                .bind()
        }
}
```

#### Command Application Errors (wraps domain errors)
```kotlin
sealed interface OrderSaveError : ApplicationError {
    data class InvalidInput(val cause: OrderError) : OrderSaveError {
        override val message = cause.message
    }
    data class SaveFailed(val cause: OrderError) : OrderSaveError {
        override val message = cause.message
    }
}
```

### Query Side (bypasses domain)

#### Query Repository Interface (defined in `application/query/repository/`)
```kotlin
interface OrderQueryRepository {
    suspend fun findById(id: Long): Either<OrderFindByIdQueryError, OrderQueryDto>
    suspend fun list(limit: Int, offset: Int): Either<OrderListQueryError, PageDto<OrderQueryDto>>
}
```

#### Query DTO (flat primitives — no domain types)
```kotlin
data class OrderQueryDto(
    val id: Long,
    val customerId: Long,
    val status: String,
    val createdAt: OffsetDateTime,
)

data class PageDto<T>(
    val items: List<T>,
    val totalCount: Long,
)
```

#### Query Use Case Interface
```kotlin
interface OrderFindByIdQueryUseCase {
    suspend fun execute(id: Long): Either<OrderFindByIdQueryError, OrderQueryDto>
}
```

#### Query Use Case Implementation
```kotlin
class OrderFindByIdQueryUseCaseImpl(
    private val queryRepository: OrderQueryRepository, // query repository (read-only)
) : OrderFindByIdQueryUseCase {
    override suspend fun execute(id: Long): Either<OrderFindByIdQueryError, OrderQueryDto> =
        queryRepository.findById(id)
}
```

#### Query Application Errors (standalone — not wrapping domain errors)
```kotlin
sealed interface OrderFindByIdQueryError : ApplicationError {
    data class NotFound(val id: Long) : OrderFindByIdQueryError {
        override val message = "Order not found: $id"
    }
    data class FetchFailed(override val message: String) : OrderFindByIdQueryError
}
```

### Shared Base
```kotlin
interface ApplicationError {
    val message: String
}
```

---

## Layer 3: Infrastructure (CQRS)

**Purpose**: Implements domain repository interfaces (command) AND application query repository interfaces (query).

**Allowed dependencies**:
- Application layer (which includes domain)
- Spring Boot, Spring Data R2DBC
- **jOOQ** (DDL-based codegen) — see `jooq-ddl` skill for details
- External SDKs (HTTP clients, messaging)

**This project uses jOOQ + R2DBC (NOT JPA, NOT Spring Data Repositories)**.

**Package structure**:
```
infrastructure/
├── command/repository/  # Command repo implementations (implements domain/ interfaces)
└── query/repository/   # Query repo implementations (implements application/query/repository/ interfaces)
```

### Command Repository (`infrastructure/command/repository/`)

Implements domain repository (write-only):

```kotlin
@Repository
class OrderRepositoryImpl(
    private val dsl: DSLContext,
) : OrderRepository {

    override suspend fun save(order: Order): Either<OrderError, Order> =
        Either.catch {
            Mono.from(
                dsl.insertInto(ORDER)
                    .set(ORDER.ID, order.id.value)
                    .set(ORDER.STATUS, order.status.name)
                    .returning()
            ).map { it.toDomain() }
                .awaitSingle()
        }.mapLeft { OrderError.RepositoryError("save failed: ${it.message}", it) }
}
```

### Query Repository (`infrastructure/query/repository/`)

Implements application query repository (read-only, returns flat DTOs):

```kotlin
@Repository
class OrderQueryRepositoryImpl(
    private val dsl: DSLContext,
) : OrderQueryRepository {

    override suspend fun findById(id: Long): Either<OrderFindByIdQueryError, OrderQueryDto> =
        either {
            val record = Either.catch {
                Mono.from(
                    dsl.selectFrom(ORDER).where(ORDER.ID.eq(id))
                ).awaitSingleOrNull()
            }.mapLeft { OrderFindByIdQueryError.FetchFailed("findById failed: ${it.message}") }
                .bind()

            if (record == null) raise(OrderFindByIdQueryError.NotFound(id))
            record.toQueryDto()  // maps to flat DTO, NOT domain entity
        }

    override suspend fun list(limit: Int, offset: Int): Either<OrderListQueryError, PageDto<OrderQueryDto>> =
        Either.catch {
            val items = Flux.from(
                dsl.selectFrom(ORDER).limit(limit).offset(offset)
            ).map { it.toQueryDto() }.collectList().awaitSingle()

            val total = Mono.from(
                dsl.selectCount().from(ORDER)
            ).awaitSingle().value1().toLong()

            PageDto(items = items, totalCount = total)
        }.mapLeft { OrderListQueryError.FetchFailed("list failed: ${it.message}") }
}
```

### DB Record → Domain mapping (command side)
```kotlin
private fun OrderRecord.toDomain() = Order(
    id = OrderId(id!!),
    customerId = CustomerId(customerId!!),
    status = OrderStatus.valueOf(status!!),
    createdAt = createdAt!!,
)
```

### DB Record → Query DTO mapping (query side)
```kotlin
private fun OrderRecord.toQueryDto() = OrderQueryDto(
    id = id!!,
    customerId = customerId!!,
    status = status!!,
    createdAt = createdAt!!,
)
```

See the `jooq-ddl` skill for:
- How to run `./gradlew generateJooq` to generate table classes from Flyway migrations
- `RenderNameCase` / `RenderQuotedNames` settings for identifier case sensitivity
- Reactive `Flux.from()` / `Mono.from()` patterns for non-blocking queries

---

## Layer 4: Presentation

**Purpose**: Adapts external requests to application use cases.

**Allowed dependencies**:
- Application layer
- Spring WebFlux (`@RestController`)
- Netflix DGS for GraphQL (`@DgsComponent`) — if this project uses GraphQL
- `*.domain.*` is allowed for DTO mapping (reading entities, value objects, error types)

**`throw` IS ALLOWED** here for framework exceptions (`GraphQLException`, `ResponseStatusException`).

### REST Controllers (inject both command and query use cases)
```kotlin
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderSaveUseCase: OrderSaveUseCase,           // command
    private val orderFindByIdQueryUseCase: OrderFindByIdQueryUseCase, // query
    private val orderListQueryUseCase: OrderListQueryUseCase,         // query
) {
    @PostMapping
    suspend fun create(@RequestBody request: OrderCreateRequest): ResponseEntity<*> =
        orderSaveUseCase.execute(request.toDto())
            .fold(
                ifLeft = { error ->
                    when (error) {
                        is OrderSaveError.InvalidInput ->
                            ResponseEntity.badRequest().body(ErrorResponse(error.message))
                        is OrderSaveError.SaveFailed -> {
                            logger.error("Save failed: ${error.message}")
                            ResponseEntity.internalServerError().body(ErrorResponse("Internal error"))
                        }
                    }
                },
                ifRight = { order -> ResponseEntity.ok(order.toResponse()) },
            )

    @GetMapping("/{id}")
    suspend fun findById(@PathVariable id: Long): ResponseEntity<*> =
        orderFindByIdQueryUseCase.execute(id)
            .fold(
                ifLeft = { error ->
                    when (error) {
                        is OrderFindByIdQueryError.NotFound ->
                            ResponseEntity.status(404).body(ErrorResponse(error.message))
                        is OrderFindByIdQueryError.FetchFailed ->
                            ResponseEntity.internalServerError().body(ErrorResponse("Internal error"))
                    }
                },
                ifRight = { dto -> ResponseEntity.ok(dto) },
            )
}
```

### GraphQL DataFetchers (Netflix DGS)
```kotlin
// Use @DgsComponent (NOT @Component) for Netflix DGS DataFetchers
@DgsComponent
class OrderDataFetcher(
    private val orderFindByIdQueryUseCase: OrderFindByIdQueryUseCase, // query
    private val orderSaveUseCase: OrderSaveUseCase,                   // command
) {
    @DgsQuery(field = "order")
    suspend fun order(@InputArgument id: Long): OrderType? =
        orderFindByIdQueryUseCase.execute(id).fold(
            ifLeft = { error ->
                when (error) {
                    is OrderFindByIdQueryError.NotFound -> null
                    is OrderFindByIdQueryError.FetchFailed -> throw GraphQLException(error.message)
                }
            },
            ifRight = { dto -> dto.toGraphQL() },
        )
}
```

---

## Layer 5: Framework (CQRS wiring)

**Purpose**: Spring Boot wiring — main class, DI configuration, Bean definitions.

**What belongs here**:
- `@SpringBootApplication` main class
- `@Configuration` classes for Bean definitions
- `@Bean` factory methods that wire both command and query use cases
- Application YAML / properties

```kotlin
@Configuration
class UseCaseConfig {
    // Command use cases (wired with domain repository)
    @Bean
    fun orderSaveUseCase(
        orderRepository: OrderRepository,  // domain repo (write-only)
    ): OrderSaveUseCase = OrderSaveUseCaseImpl(orderRepository)

    // Query use cases (wired with query repository)
    @Bean
    fun orderFindByIdQueryUseCase(
        queryRepository: OrderQueryRepository,  // query repo (read-only)
    ): OrderFindByIdQueryUseCase = OrderFindByIdQueryUseCaseImpl(queryRepository)

    @Bean
    fun orderListQueryUseCase(
        queryRepository: OrderQueryRepository,
    ): OrderListQueryUseCase = OrderListQueryUseCaseImpl(queryRepository)

    // NOTE: Use case classes do NOT have @Service/@Component — they are wired here manually
}
```
