package com.wakita181009.cleanarchitecture.domain.valueobject

import arrow.core.raise.either
import arrow.core.raise.ensure
import com.wakita181009.cleanarchitecture.domain.error.PageNumberError

@JvmInline
value class PageNumber private constructor(
    val value: Int,
) {
    companion object {
        const val MIN_VALUE: Int = 1

        operator fun invoke(value: Int) = PageNumber(value)

        fun of(value: Int) =
            either {
                ensure(value >= MIN_VALUE) { PageNumberError.BelowMinimum(value) }
                PageNumber(value)
            }
    }
}
