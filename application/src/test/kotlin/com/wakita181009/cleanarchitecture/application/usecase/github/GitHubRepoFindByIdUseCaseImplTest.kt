package com.wakita181009.cleanarchitecture.application.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoFindByIdError
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository
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

class GitHubRepoFindByIdUseCaseImplTest {
    private val repository = mockk<GitHubRepoRepository>()
    private val useCase = GitHubRepoFindByIdUseCaseImpl(repository)

    @Test
    fun `execute returns Right with repo when found`() =
        runTest {
            val repo = sampleRepo()
            coEvery { repository.findById(any()) } returns Either.Right(repo)

            useCase.execute(1L).shouldBeRight(repo)
        }

    @Test
    fun `execute returns Left InvalidId when id is not positive`() =
        runTest {
            val error = useCase.execute(0L).shouldBeLeft()
            error shouldBe GitHubRepoFindByIdError.InvalidId(GitHubError.InvalidId(0L))
        }

    @Test
    fun `execute returns Left NotFound when repo is not found`() =
        runTest {
            val domainError = GitHubError.NotFound(GitHubRepoId(1L))
            coEvery { repository.findById(any()) } returns Either.Left(domainError)

            val error = useCase.execute(1L).shouldBeLeft()
            error shouldBe GitHubRepoFindByIdError.NotFound(domainError)
        }

    @Test
    fun `execute returns Left FetchFailed when RepositoryError occurs`() =
        runTest {
            val domainError = GitHubError.RepositoryError("DB connection failed")
            coEvery { repository.findById(any()) } returns Either.Left(domainError)

            val error = useCase.execute(1L).shouldBeLeft()
            error shouldBe GitHubRepoFindByIdError.FetchFailed(domainError)
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
