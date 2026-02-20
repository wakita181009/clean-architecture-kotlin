package com.wakita181009.cleanarchitecture.domain.valueobject

import com.wakita181009.cleanarchitecture.domain.error.PageNumberError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PageNumberTest {
    @Test
    fun `of returns Right for value equal to minimum`() {
        val result = PageNumber.of(1)
        result.shouldBeRight()
    }

    @Test
    fun `of returns Right value wraps the input`() {
        val result = PageNumber.of(10)
        val pageNumber = result.shouldBeRight()
        pageNumber.value shouldBe 10
    }

    @Test
    fun `of returns Right for large value`() {
        PageNumber.of(1000).shouldBeRight()
    }

    @Test
    fun `of returns Left BelowMinimum when value is 0`() {
        val error = PageNumber.of(0).shouldBeLeft()
        error shouldBe PageNumberError.BelowMinimum(0)
    }

    @Test
    fun `of returns Left BelowMinimum when value is negative`() {
        val error = PageNumber.of(-5).shouldBeLeft()
        error shouldBe PageNumberError.BelowMinimum(-5)
    }

    @Test
    fun `BelowMinimum error message contains the invalid value`() {
        val error = PageNumberError.BelowMinimum(0)
        error.message shouldBe "Page number must be at least ${PageNumber.MIN_VALUE}, but was 0"
    }
}
