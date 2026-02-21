# Test Patterns for Kotlin CA/FP

## Test Dependencies

```kotlin
// Per layer build.gradle.kts
testImplementation(kotlin("test"))
testImplementation(libs.kotest.assertions.core)
testImplementation(libs.kotest.assertions.arrow)   // shouldBeRight, shouldBeLeft
testImplementation(libs.kotest.property)            // checkAll, Arb
testImplementation(libs.mockk)                      // mockk, coEvery
testImplementation(libs.kotlinx.coroutines.test)    // runTest
```

## Domain Layer Tests

### Value Object — Happy Path + Boundary (Property-Based)

```kotlin
class OrderIdTest {
    @Test
    fun `of returns Right with value for any positive Long`() =
        runTest {
            checkAll(Arb.long(min = 1L)) { n ->
                OrderId.of(n).shouldBeRight().value shouldBe n
            }
        }

    @Test
    fun `of returns Left InvalidId for zero and negative values`() =
        runTest {
            checkAll(Arb.long(max = 0L)) { n ->
                OrderId.of(n)
                    .shouldBeLeft()
                    .shouldBeInstanceOf<OrderError.InvalidId>()
            }
        }

    @Test
    fun `InvalidId error message contains the invalid value`() {
        val error = OrderError.InvalidId(-5L)
        error.message shouldContain "-5"
    }
}
```

### Value Object — Boundary Conditions

```kotlin
class PageSizeTest {
    @Test
    fun `of returns Right for boundary values 1 and 100`() =
        runTest {
            PageSize.of(1).shouldBeRight().value shouldBe 1
            PageSize.of(100).shouldBeRight().value shouldBe 100
        }

    @Test
    fun `of returns Left BelowMinimum when value is 0`() =
        runTest {
            PageSize.of(0).shouldBeLeft() shouldBe PageSizeError.BelowMinimum(0)
        }

    @Test
    fun `of returns Left AboveMaximum when value is 101`() =
        runTest {
            PageSize.of(101).shouldBeLeft() shouldBe PageSizeError.AboveMaximum(101)
        }

    @Test
    fun `of returns Right for any value in range 1..100`() =
        runTest {
            checkAll(Arb.int(1..100)) { n ->
                PageSize.of(n).shouldBeRight()
            }
        }
}
```

---

## Application Layer Tests — CQRS (Use Cases with MockK)

### Command Use Case Tests (writes through domain)

```kotlin
class OrderSaveUseCaseImplTest {
    // Mock the domain repository (write-only)
    private val orderRepository = mockk<OrderRepository>()

    // Instantiate with mocked dependencies (no Spring context needed)
    private val useCase = OrderSaveUseCaseImpl(orderRepository)

    @Test
    fun `execute returns Right with saved order when DTO is valid`() =
        runTest {
            val savedOrder = sampleOrder()
            coEvery { orderRepository.save(any()) } returns Either.Right(savedOrder)

            useCase.execute(sampleDto()).shouldBeRight(savedOrder)

            coVerify(exactly = 1) { orderRepository.save(any()) }
        }

    @Test
    fun `execute returns Left InvalidInput when DTO has invalid data`() =
        runTest {
            val result = useCase.execute(sampleDto().copy(id = -1L))
            result.shouldBeLeft().shouldBeInstanceOf<OrderSaveError.InvalidInput>()

            // Repository.save should NOT be called when validation fails
            coVerify(exactly = 0) { orderRepository.save(any()) }
        }

    @Test
    fun `execute returns Left SaveFailed when repository returns error`() =
        runTest {
            coEvery { orderRepository.save(any()) } returns
                Either.Left(OrderError.RepositoryError("DB error"))

            useCase.execute(sampleDto())
                .shouldBeLeft()
                .shouldBeInstanceOf<OrderSaveError.SaveFailed>()
        }
}
```

### Query Use Case Tests (bypasses domain, uses query repository)

```kotlin
class OrderFindByIdQueryUseCaseImplTest {
    // Mock the query repository (read-only, returns flat DTOs)
    private val queryRepository = mockk<OrderQueryRepository>()

    private val useCase = OrderFindByIdQueryUseCaseImpl(queryRepository)

    @Test
    fun `execute returns Right with query DTO when record exists`() =
        runTest {
            val dto = sampleQueryDto()
            coEvery { queryRepository.findById(1L) } returns Either.Right(dto)

            useCase.execute(1L).shouldBeRight(dto)
        }

    @Test
    fun `execute returns Left NotFound when record does not exist`() =
        runTest {
            coEvery { queryRepository.findById(999L) } returns
                Either.Left(OrderFindByIdQueryError.NotFound(999L))

            useCase.execute(999L)
                .shouldBeLeft()
                .shouldBeInstanceOf<OrderFindByIdQueryError.NotFound>()
        }

    @Test
    fun `execute returns Left FetchFailed when query fails`() =
        runTest {
            coEvery { queryRepository.findById(any()) } returns
                Either.Left(OrderFindByIdQueryError.FetchFailed("DB error"))

            useCase.execute(1L)
                .shouldBeLeft()
                .shouldBeInstanceOf<OrderFindByIdQueryError.FetchFailed>()
        }
}
```

### Test Helper Functions

```kotlin
private fun sampleOrder() = Order(
    id = OrderId(100L),
    customerId = CustomerId(1L),
    status = OrderStatus.PENDING,
    createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
)

private fun sampleDto() = OrderDto(
    id = 100L,
    customerId = 1L,
    status = "PENDING",
    createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
)

private fun sampleQueryDto() = OrderQueryDto(
    id = 100L,
    customerId = 1L,
    status = "PENDING",
    createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
)
```

---

## Presentation Layer Tests — CQRS (Controller with MockK)

Controller injects both command and query use cases:

```kotlin
class OrderControllerTest {
    // Command use case
    private val saveUseCase = mockk<OrderSaveUseCase>()
    // Query use cases
    private val findByIdQueryUseCase = mockk<OrderFindByIdQueryUseCase>()
    private val listQueryUseCase = mockk<OrderListQueryUseCase>()

    private val controller = OrderController(saveUseCase, findByIdQueryUseCase, listQueryUseCase)

    // --- Command endpoint tests ---
    @Test
    fun `create returns 200 with order body when save succeeds`() =
        runTest {
            val order = sampleOrder()
            coEvery { saveUseCase.execute(any()) } returns Either.Right(order)

            val response = controller.create(sampleCreateRequest())
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldBe order.toResponse()
        }

    @Test
    fun `create returns 400 when input is invalid`() =
        runTest {
            val error = OrderSaveError.InvalidInput(OrderError.InvalidId(-1L))
            coEvery { saveUseCase.execute(any()) } returns Either.Left(error)

            val response = controller.create(sampleCreateRequest())
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            (response.body as ErrorResponse).message shouldBe error.message
        }

    @Test
    fun `create returns 500 when save fails unexpectedly`() =
        runTest {
            val error = OrderSaveError.SaveFailed(OrderError.RepositoryError("DB error"))
            coEvery { saveUseCase.execute(any()) } returns Either.Left(error)

            val response = controller.create(sampleCreateRequest())
            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        }

    // --- Query endpoint tests ---
    @Test
    fun `findById returns 200 with query DTO when record exists`() =
        runTest {
            val dto = sampleQueryDto()
            coEvery { findByIdQueryUseCase.execute(1L) } returns Either.Right(dto)

            val response = controller.findById(1L)
            response.statusCode shouldBe HttpStatus.OK
        }

    @Test
    fun `findById returns 404 when order does not exist`() =
        runTest {
            val error = OrderFindByIdQueryError.NotFound(999L)
            coEvery { findByIdQueryUseCase.execute(999L) } returns Either.Left(error)

            val response = controller.findById(999L)
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
}
```

---

## Common Kotest Arrow Assertions

```kotlin
// Assert Right with specific value
result.shouldBeRight(expectedValue)

// Assert Right and get value
val value = result.shouldBeRight()
value.someField shouldBe expected

// Assert Left with specific value
result.shouldBeLeft(expectedError)

// Assert Left and check type
result.shouldBeLeft().shouldBeInstanceOf<SpecificError>()

// Assert Left and get value
val error = result.shouldBeLeft()
error.message shouldContain "expected text"
```

---

## MockK Patterns for Coroutines

```kotlin
// Mock setup
val mock = mockk<SomeInterface>()

// Mock suspend function
coEvery { mock.suspendMethod(any()) } returns Either.Right(value)
coEvery { mock.suspendMethod(specificArg) } returns Either.Left(error)

// Verify calls
coVerify(exactly = 1) { mock.suspendMethod(any()) }
coVerify(exactly = 0) { mock.anotherMethod(any()) }  // should NOT be called

// Argument matchers
coEvery { mock.findById(match { it.value == 1L }) } returns Either.Right(entity)
coEvery { mock.save(any()) } answers { Either.Right(firstArg()) }  // return the first argument
```
