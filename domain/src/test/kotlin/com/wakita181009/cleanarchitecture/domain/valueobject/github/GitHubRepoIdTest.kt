package com.wakita181009.cleanarchitecture.domain.valueobject.github

import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GitHubRepoIdTest {
    @Test
    fun `of returns Right with correct value for any positive id`() =
        runTest {
            checkAll(Arb.long(min = 1L)) { n ->
                GitHubRepoId.of(n).shouldBeRight().value shouldBe n
            }
        }

    @Test
    fun `of returns Left InvalidId for any non-positive id`() =
        runTest {
            checkAll(Arb.long(max = 0L)) { n ->
                GitHubRepoId.of(n).shouldBeLeft().shouldBeInstanceOf<GitHubError.InvalidId>()
            }
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
