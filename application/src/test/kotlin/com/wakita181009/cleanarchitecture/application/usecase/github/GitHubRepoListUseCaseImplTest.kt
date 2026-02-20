package com.wakita181009.cleanarchitecture.application.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoListError
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.error.PageNumberError
import com.wakita181009.cleanarchitecture.domain.error.PageSizeError
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository
import com.wakita181009.cleanarchitecture.domain.valueobject.Page
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubOwner
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoName
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.OffsetDateTime
import kotlin.test.Test

class GitHubRepoListUseCaseImplTest {
    private val repository = mockk<GitHubRepoRepository>()
    private val useCase = GitHubRepoListUseCaseImpl(repository)

    @Test
    fun `execute returns Right with page when parameters are valid`() =
        runTest {
            val page = Page(totalCount = 1, items = listOf(sampleRepo()))
            coEvery { repository.list(any(), any()) } returns Either.Right(page)

            useCase.execute(1, 20).shouldBeRight(page)
        }

    @Test
    fun `execute returns Right with empty page when repository returns empty`() =
        runTest {
            val emptyPage = Page<GitHubRepo>(totalCount = 0, items = emptyList())
            coEvery { repository.list(any(), any()) } returns Either.Right(emptyPage)

            useCase.execute(1, 20).shouldBeRight(emptyPage)
        }

    @Test
    fun `execute returns Left InvalidPageNumber when pageNumber is 0`() =
        runTest {
            val error = useCase.execute(0, 20).shouldBeLeft()
            error shouldBe GitHubRepoListError.InvalidPageNumber(PageNumberError.BelowMinimum(0))
        }

    @Test
    fun `execute returns Left InvalidPageNumber when pageNumber is negative`() =
        runTest {
            val error = useCase.execute(-1, 20).shouldBeLeft()
            error shouldBe GitHubRepoListError.InvalidPageNumber(PageNumberError.BelowMinimum(-1))
        }

    @Test
    fun `execute returns Left InvalidPageSize when pageSize is 0`() =
        runTest {
            val error = useCase.execute(1, 0).shouldBeLeft()
            error shouldBe GitHubRepoListError.InvalidPageSize(PageSizeError.BelowMinimum(0))
        }

    @Test
    fun `execute returns Left InvalidPageSize when pageSize exceeds 100`() =
        runTest {
            val error = useCase.execute(1, 101).shouldBeLeft()
            error shouldBe GitHubRepoListError.InvalidPageSize(PageSizeError.AboveMaximum(101))
        }

    @Test
    fun `execute returns Left FetchFailed when repository returns error`() =
        runTest {
            val domainError = GitHubError.RepositoryError("DB error")
            coEvery { repository.list(any(), any()) } returns Either.Left(domainError)

            val error = useCase.execute(1, 20).shouldBeLeft()
            error shouldBe GitHubRepoListError.FetchFailed(domainError)
        }
}

private fun sampleRepo() =
    GitHubRepo(
        id = GitHubRepoId(1L),
        owner = GitHubOwner("octocat"),
        name = GitHubRepoName("hello-world"),
        fullName = "octocat/hello-world",
        description = null,
        language = "Kotlin",
        stargazersCount = 0,
        forksCount = 0,
        isPrivate = false,
        createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
        updatedAt = OffsetDateTime.parse("2024-01-02T00:00:00Z"),
    )
