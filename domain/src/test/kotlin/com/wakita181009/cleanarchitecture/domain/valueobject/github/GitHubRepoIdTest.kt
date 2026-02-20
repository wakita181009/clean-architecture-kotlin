package com.wakita181009.cleanarchitecture.domain.valueobject.github

import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class GitHubRepoIdTest {
    @Test
    fun `of returns Right for valid numeric string`() {
        val result = GitHubRepoId.of("123")
        val id = result.shouldBeRight()
        id.value shouldBe 123L
    }

    @Test
    fun `of returns Right for large numeric string`() {
        val result = GitHubRepoId.of("9999999")
        val id = result.shouldBeRight()
        id.value shouldBe 9999999L
    }

    @Test
    fun `of returns Left InvalidId for non-numeric string`() {
        val error = GitHubRepoId.of("abc").shouldBeLeft()
        error.shouldBeInstanceOf<GitHubError.InvalidId>()
    }

    @Test
    fun `of returns Left InvalidId for empty string`() {
        val error = GitHubRepoId.of("").shouldBeLeft()
        error.shouldBeInstanceOf<GitHubError.InvalidId>()
    }

    @Test
    fun `of returns Left InvalidId for decimal string`() {
        val error = GitHubRepoId.of("1.5").shouldBeLeft()
        error.shouldBeInstanceOf<GitHubError.InvalidId>()
    }

    @Test
    fun `InvalidId error message is set correctly`() {
        val error = GitHubError.InvalidId()
        error.message shouldBe "Invalid GitHub repo ID format"
    }

    @Test
    fun `NotFound error message contains the id`() {
        val error = GitHubError.NotFound(42L)
        error.message shouldBe "GitHub repo not found: 42"
    }
}
