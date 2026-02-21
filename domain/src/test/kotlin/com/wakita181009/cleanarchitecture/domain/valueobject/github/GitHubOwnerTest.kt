package com.wakita181009.cleanarchitecture.domain.valueobject.github

import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class GitHubOwnerTest {
    @Test
    fun `of returns Right for valid owner`() {
        val result = GitHubOwner.of("octocat")
        val owner = result.shouldBeRight()
        owner.value shouldBe "octocat"
    }

    @Test
    fun `of returns Left InvalidOwner for blank string`() {
        val error = GitHubOwner.of("").shouldBeLeft()
        error.shouldBeInstanceOf<GitHubError.InvalidOwner>()
    }

    @Test
    fun `of returns Left InvalidOwner for whitespace-only string`() {
        val error = GitHubOwner.of("   ").shouldBeLeft()
        error.shouldBeInstanceOf<GitHubError.InvalidOwner>()
    }

    @Test
    fun `InvalidOwner error message is correct`() {
        GitHubError.InvalidOwner.message shouldBe "GitHub owner must not be blank"
    }
}
