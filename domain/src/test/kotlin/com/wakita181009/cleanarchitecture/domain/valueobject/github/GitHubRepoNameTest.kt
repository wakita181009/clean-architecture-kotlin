package com.wakita181009.cleanarchitecture.domain.valueobject.github

import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class GitHubRepoNameTest {
    @Test
    fun `of returns Right for valid name`() {
        val result = GitHubRepoName.of("hello-world")
        val name = result.shouldBeRight()
        name.value shouldBe "hello-world"
    }

    @Test
    fun `of returns Left InvalidName for blank string`() {
        val error = GitHubRepoName.of("").shouldBeLeft()
        error.shouldBeInstanceOf<GitHubError.InvalidName>()
    }

    @Test
    fun `of returns Left InvalidName for whitespace-only string`() {
        val error = GitHubRepoName.of("   ").shouldBeLeft()
        error.shouldBeInstanceOf<GitHubError.InvalidName>()
    }

    @Test
    fun `InvalidName error message is correct`() {
        GitHubError.InvalidName.message shouldBe "GitHub repo name must not be blank"
    }
}
