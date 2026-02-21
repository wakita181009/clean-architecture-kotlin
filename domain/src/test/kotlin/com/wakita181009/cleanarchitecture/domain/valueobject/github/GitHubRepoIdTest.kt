package com.wakita181009.cleanarchitecture.domain.valueobject.github

import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class GitHubRepoIdTest {
    @Test
    fun `of returns Right for minimum valid id`() {
        val result = GitHubRepoId.of(1L)
        val id = result.shouldBeRight()
        id.value shouldBe 1L
    }

    @Test
    fun `of returns Right for large id`() {
        val result = GitHubRepoId.of(9999999L)
        val id = result.shouldBeRight()
        id.value shouldBe 9999999L
    }

    @Test
    fun `of returns Left InvalidId for zero`() {
        val error = GitHubRepoId.of(0L).shouldBeLeft()
        error.shouldBeInstanceOf<GitHubError.InvalidId>()
    }

    @Test
    fun `of returns Left InvalidId for negative id`() {
        val error = GitHubRepoId.of(-1L).shouldBeLeft()
        error.shouldBeInstanceOf<GitHubError.InvalidId>()
    }

    @Test
    fun `InvalidId error message contains the invalid value`() {
        val error = GitHubError.InvalidId(-1L)
        error.message shouldBe "Invalid GitHub repo ID: -1 (must be positive)"
    }

    @Test
    fun `NotFound error message contains the id`() {
        val error = GitHubError.NotFound(GitHubRepoId(42L))
        error.message shouldBe "GitHub repo not found: 42"
    }
}
