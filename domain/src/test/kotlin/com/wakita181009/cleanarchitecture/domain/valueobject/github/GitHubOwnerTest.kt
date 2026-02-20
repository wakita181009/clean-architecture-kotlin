package com.wakita181009.cleanarchitecture.domain.valueobject.github

import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GitHubOwnerTest {
    @Test
    fun `of returns Right with correct value for any non-blank string`() =
        runTest {
            checkAll(Arb.string(1..100).filter { it.isNotBlank() }) { s ->
                GitHubOwner.of(s).shouldBeRight().value shouldBe s
            }
        }

    @Test
    fun `of returns Left InvalidOwner for any blank string`() =
        runTest {
            checkAll(Arb.of("", " ", "   ", "\t", "\n", "\r\n", "\u00A0")) { s ->
                GitHubOwner.of(s).shouldBeLeft().shouldBeInstanceOf<GitHubError.InvalidOwner>()
            }
        }

    @Test
    fun `InvalidOwner error message is correct`() {
        GitHubError.InvalidOwner.message shouldBe "GitHub owner must not be blank"
    }
}
