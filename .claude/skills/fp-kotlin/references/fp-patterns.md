# Functional Programming Patterns in Kotlin with Arrow-kt

## 1. Make Illegal States Unrepresentable

Use the type system to prevent invalid data at compile time.

```kotlin
// BAD: Nothing prevents creating an Order with negative amount or empty customer name
data class Order(val customerId: Long, val amount: Double, val customerName: String)

// GOOD: Types carry their own invariants
data class Order(
    val customerId: CustomerId,   // Can only be positive Long
    val amount: Money,            // Can only be non-negative BigDecimal
    val customerName: CustomerName,  // Can only be non-blank, max 100 chars
)
```

## 2. Pure Functions

A pure function has no side effects and returns the same output for the same input.

```kotlin
// PURE: Given same input, always same output; no mutations, no I/O
fun calculateDiscount(price: Money, rate: DiscountRate): Money =
    Money(price.value * (1 - rate.value))

// IMPURE: Has side effects (DB call, random, current time)
// → Move to infrastructure layer, return Either<Error, T>
suspend fun findOrder(id: OrderId): Either<OrderError, Order> = ...
```

## 3. Immutability Patterns

```kotlin
// data class: structural equality + copy()
data class Order(val status: OrderStatus, val items: List<OrderItem>)

// Update via copy() — never mutate
fun Order.confirm(): Either<OrderError, Order> =
    either {
        ensure(status == OrderStatus.PENDING) { OrderError.CannotConfirm(status) }
        copy(status = OrderStatus.CONFIRMED)  // Returns new instance
    }

// @JvmInline value class: zero-cost wrapper with full type safety
@JvmInline
value class Money private constructor(val value: BigDecimal) {
    operator fun plus(other: Money) = Money(value + other.value)
    operator fun times(factor: BigDecimal) = Money(value * factor)

    companion object {
        fun of(v: BigDecimal): Either<MoneyError, Money> =
            either {
                ensure(v >= BigDecimal.ZERO) { MoneyError.NegativeAmount(v) }
                Money(v)
            }
        operator fun invoke(v: BigDecimal) = Money(v)  // Internal use
    }
}
```

## 4. Extension Functions for Layer Mapping

Use extension functions to keep mapping logic close to the class, without polluting the class itself.

```kotlin
// Domain → Presentation (in presentation layer)
fun Order.toResponse() = OrderResponse(
    id = id.value,
    status = status.name,
    totalAmount = items.sumOf { it.unitPrice.value * it.quantity.value.toBigDecimal() },
    createdAt = createdAt,
)

// Presentation → Application (in presentation layer)
fun OrderCreateRequest.toDto() = OrderCreateDto(
    customerId = customerId,
    items = items.map { it.toDto() },
)

// DB Record → Domain (in infrastructure layer, companion or file-level)
fun OrderRecord.toDomain() = Order(
    id = OrderId(id!!),
    customerId = CustomerId(customerId!!),
    status = OrderStatus.valueOf(status!!),
    items = emptyList(),
    createdAt = createdAt!!,
)
```

## 5. Domain Entity Behavior (Methods on Entities)

Place business logic as methods on domain entities:

```kotlin
data class Order(
    val id: OrderId,
    val status: OrderStatus,
    val items: List<OrderItem>,
) {
    // Business rule: can only cancel PENDING orders
    fun cancel(): Either<OrderError, Order> =
        either {
            ensure(status == OrderStatus.PENDING) { OrderError.CannotCancel(status) }
            copy(status = OrderStatus.CANCELLED)
        }

    // Pure computation — no side effects
    fun totalAmount(): Money =
        items.fold(Money(BigDecimal.ZERO)) { acc, item ->
            acc + (item.unitPrice * item.quantity.value.toBigDecimal())
        }
}
```

## 6. Sealed Interface Error Hierarchy Design

Design errors to carry just enough information for recovery or logging:

```kotlin
// Layer marker interfaces
interface DomainError { val message: String }
interface ApplicationError { val message: String }

// Domain errors: specific to the domain concept
sealed interface OrderError : DomainError {
    // Value error: carries the invalid value
    data class InvalidId(val value: Long) : OrderError {
        override val message = "Invalid order ID: $value (must be positive)"
    }
    // State error: carries the current state
    data class CannotCancel(val currentStatus: OrderStatus) : OrderError {
        override val message = "Cannot cancel order with status: $currentStatus"
    }
    // Not found: carries the ID that wasn't found
    data class NotFound(val id: OrderId) : OrderError {
        override val message = "Order not found: ${id.value}"
    }
    // Infrastructure error: wraps the original exception for logging
    data class RepositoryError(override val message: String, val cause: Throwable? = null) : OrderError
}

// Application errors: one sealed interface per use case
sealed interface OrderCancelError : ApplicationError {
    data class InvalidId(val cause: OrderError.InvalidId) : OrderCancelError {
        override val message = cause.message
    }
    data class NotFound(val cause: OrderError.NotFound) : OrderCancelError {
        override val message = cause.message
    }
    data class AlreadyCancelled(val cause: OrderError.CannotCancel) : OrderCancelError {
        override val message = cause.message
    }
    data class SaveFailed(val cause: OrderError) : OrderCancelError {
        override val message = "Failed to save cancelled order: ${cause.message}"
    }
}
```

## 7. Function Composition

```kotlin
// Chain transformations without intermediate variables
fun processOrderRequest(request: OrderCreateRequest): Either<OrderCreateError, OrderResponse> =
    request.toDto()
        .let { dto -> orderCreateUseCase.execute(dto) }
        .map { order -> order.toResponse() }

// Using let/run for local scoping
val result = orderRepository.findById(id)
    .map { order ->
        order.copy(status = OrderStatus.CONFIRMED)
    }
    .flatMap { confirmedOrder ->
        orderRepository.save(confirmedOrder)
    }
```

## 8. Avoiding `null` with Explicit Optionality

```kotlin
// Prefer Either<NotFound, T> over T?
// This makes the "not found" case explicit and documented

// BAD: null propagation is implicit
suspend fun findOrder(id: OrderId): Order? = ...

// GOOD: error is explicit and typed
suspend fun findById(id: OrderId): Either<OrderError, Order> = ...

// For truly optional data (no error semantics), use nullable with documentation
data class Order(
    val id: OrderId,
    val description: String?,  // Optional — null means "no description provided"
    val language: String?,     // Optional — null means "language unknown"
)
```
