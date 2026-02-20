package com.wakita181009.cleanarchitecture.domain.valueobject

import com.wakita181009.cleanarchitecture.domain.error.PageSizeError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PageSizeTest {
    @Test
    fun `of returns Right with correct value for any value in valid range`() =
        runTest {
            checkAll(Arb.int(PageSize.MIN_VALUE..PageSize.MAX_VALUE)) { n ->
                PageSize.of(n).shouldBeRight().value shouldBe n
            }
        }

    @Test
    fun `of returns Left BelowMinimum for any value below minimum`() =
        runTest {
            checkAll(Arb.int(max = PageSize.MIN_VALUE - 1)) { n ->
                PageSize.of(n).shouldBeLeft() shouldBe PageSizeError.BelowMinimum(n)
            }
        }

    @Test
    fun `of returns Left AboveMaximum for any value above maximum`() =
        runTest {
            checkAll(Arb.int(min = PageSize.MAX_VALUE + 1)) { n ->
                PageSize.of(n).shouldBeLeft() shouldBe PageSizeError.AboveMaximum(n)
            }
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
