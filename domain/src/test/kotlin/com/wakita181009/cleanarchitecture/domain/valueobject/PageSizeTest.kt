package com.wakita181009.cleanarchitecture.domain.valueobject

import com.wakita181009.cleanarchitecture.domain.error.PageSizeError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PageSizeTest {
    @Test
    fun `of returns Right for minimum value`() {
        PageSize.of(1).shouldBeRight()
    }

    @Test
    fun `of returns Right for maximum value`() {
        PageSize.of(100).shouldBeRight()
    }

    @Test
    fun `of returns Right value wraps the input`() {
        val result = PageSize.of(50)
        val pageSize = result.shouldBeRight()
        pageSize.value shouldBe 50
    }

    @Test
    fun `of returns Left BelowMinimum when value is 0`() {
        val error = PageSize.of(0).shouldBeLeft()
        error shouldBe PageSizeError.BelowMinimum(0)
    }

    @Test
    fun `of returns Left BelowMinimum when value is negative`() {
        val error = PageSize.of(-1).shouldBeLeft()
        error shouldBe PageSizeError.BelowMinimum(-1)
    }

    @Test
    fun `of returns Left AboveMaximum when value exceeds 100`() {
        val error = PageSize.of(101).shouldBeLeft()
        error shouldBe PageSizeError.AboveMaximum(101)
    }

    @Test
    fun `BelowMinimum error message contains the invalid value`() {
        val error = PageSizeError.BelowMinimum(0)
        error.message shouldBe "Page size must be at least ${PageSize.MIN_VALUE}, but was 0"
    }

    @Test
    fun `AboveMaximum error message contains the invalid value`() {
        val error = PageSizeError.AboveMaximum(200)
        error.message shouldBe "Page size must be at most ${PageSize.MAX_VALUE}, but was 200"
    }
}
