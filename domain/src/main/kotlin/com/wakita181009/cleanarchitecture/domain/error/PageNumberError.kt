package com.wakita181009.cleanarchitecture.domain.error

import com.wakita181009.cleanarchitecture.domain.valueobject.PageNumber

sealed class PageNumberError(
    message: String?,
    cause: Throwable? = null,
) : DomainError(message, cause) {
    class BelowMinimum(
        val value: Int,
    ) : PageNumberError("Page number must be at least ${PageNumber.MIN_VALUE}, but was $value")
}
