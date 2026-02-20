package com.wakita181009.cleanarchitecture.domain.error

import com.wakita181009.cleanarchitecture.domain.valueobject.PageNumber

sealed interface PageNumberError : DomainError {
    data class BelowMinimum(
        val value: Int,
    ) : PageNumberError {
        override val message = "Page number must be at least ${PageNumber.MIN_VALUE}, but was $value"
    }
}
