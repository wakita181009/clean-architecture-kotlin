package com.wakita181009.cleanarchitecture.domain.valueobject

import com.wakita181009.cleanarchitecture.domain.error.PageNumberError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PageNumberTest {
    @Test
    fun `of returns Right with correct value for any valid input`() =
        runTest {
            checkAll(Arb.int(min = PageNumber.MIN_VALUE)) { n ->
                PageNumber.of(n).shouldBeRight().value shouldBe n
            }
        }

    @Test
    fun `of returns Left BelowMinimum for any value below minimum`() =
        runTest {
            checkAll(Arb.int(max = PageNumber.MIN_VALUE - 1)) { n ->
                PageNumber.of(n).shouldBeLeft() shouldBe PageNumberError.BelowMinimum(n)
            }
        }

    @Test
    fun `BelowMinimum error message contains the invalid value`() {
        val error = PageNumberError.BelowMinimum(0)
        error.message shouldBe "Page number must be at least ${PageNumber.MIN_VALUE}, but was 0"
    }
}
