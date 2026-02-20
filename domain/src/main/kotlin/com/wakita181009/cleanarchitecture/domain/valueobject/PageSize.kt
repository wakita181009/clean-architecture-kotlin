package com.wakita181009.cleanarchitecture.domain.valueobject

import arrow.core.raise.either
import arrow.core.raise.ensure
import com.wakita181009.cleanarchitecture.domain.error.PageSizeError

@JvmInline
value class PageSize private constructor(
    val value: Int,
) {
    companion object {
        const val MIN_VALUE: Int = 1
        const val MAX_VALUE: Int = 100

        operator fun invoke(value: Int) = PageSize(value)

        fun of(value: Int) =
            either {
                ensure(value >= MIN_VALUE) { PageSizeError.BelowMinimum(value) }
                ensure(value <= MAX_VALUE) { PageSizeError.AboveMaximum(value) }
                PageSize(value)
            }
    }
}
