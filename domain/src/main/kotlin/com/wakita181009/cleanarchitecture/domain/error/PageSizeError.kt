package com.wakita181009.cleanarchitecture.domain.error

import com.wakita181009.cleanarchitecture.domain.valueobject.PageSize

sealed interface PageSizeError : DomainError {
    data class BelowMinimum(
        val value: Int,
    ) : PageSizeError {
        override val message = "Page size must be at least ${PageSize.MIN_VALUE}, but was $value"
    }

    data class AboveMaximum(
        val value: Int,
    ) : PageSizeError {
        override val message = "Page size must be at most ${PageSize.MAX_VALUE}, but was $value"
    }
}
