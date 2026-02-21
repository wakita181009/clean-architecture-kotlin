---
name: fp-kotlin
description: >
  Functional programming patterns for Kotlin with Arrow-kt. Use when writing immutable domain
  models, designing pure functions, working with Either error handling, creating value objects,
  or applying sealed interface error hierarchies. Triggers on: value object design, Either usage,
  sealed class/interface design, entity mutation patterns, extension function mapping.
---

# FP in Kotlin with Arrow-kt

## Core Principles

1. **Immutability** — `val` over `var`; entities use `.copy()` for updates; collections are `List`/`Map`
2. **Explicit Errors** — `Either<Error, Value>` instead of exceptions (except presentation layer)
3. **Type-Driven Design** — make illegal states unrepresentable via value objects
4. **Total Functions** — handle all inputs; no throwing in domain/application/infrastructure

## Value Object Pattern

```kotlin
@JvmInline
value class OrderId private constructor(val value: Long) {
    companion object {
        operator fun invoke(v: Long) = OrderId(v)  // Internal: trust caller
        fun of(v: Long): Either<OrderError, OrderId> = either {
            ensure(v > 0L) { OrderError.InvalidId(v) }
            OrderId(v)
        }
    }
}
```

## Either Quick Reference

| Pattern | Usage |
|---------|-------|
| `either { }` + `.bind()` | Chain operations, short-circuit on first Left |
| `ensure(cond) { err }` | Conditional raise inside `either {}` |
| `raise(error)` | Immediate Left inside `either {}` |
| `Either.catch { }` | Wrap exceptions from infrastructure |
| `.mapLeft { }` | Transform error type at layer boundary |
| `.fold(ifLeft, ifRight)` | Consume in presentation layer |

## Sealed Interface Errors

```kotlin
sealed interface OrderError : DomainError {
    data class InvalidId(val value: Long) : OrderError {
        override val message = "Invalid order ID: $value (must be positive)"
    }
    data object AlreadyCancelled : OrderError {
        override val message = "Order is already cancelled"
    }
    data class RepositoryError(override val message: String, val cause: Throwable? = null) : OrderError
}
```

`when` on sealed interfaces is exhaustive — no `else` branch needed.

For complete patterns (immutability, entity behavior, layer mapping, function composition): see [references/fp-patterns.md](references/fp-patterns.md).
