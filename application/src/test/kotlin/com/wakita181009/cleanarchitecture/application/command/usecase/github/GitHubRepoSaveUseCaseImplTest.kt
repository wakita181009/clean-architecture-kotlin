package com.wakita181009.cleanarchitecture.application.command.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.command.dto.github.GitHubRepoDto
import com.wakita181009.cleanarchitecture.application.command.error.github.GitHubRepoSaveError
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

class GitHubRepoSaveUseCaseImplTest {
    private val repository = mockk<GitHubRepoRepository>()
    private val useCase = GitHubRepoSaveUseCaseImpl(repository)

    @Test
    fun `execute returns Right with saved repo on success`() =
        runTest {
            val repo = sampleRepo()
            coEvery { repository.save(any()) } returns Either.Right(repo)

            useCase.execute(sampleDto()).shouldBeRight(repo)
        }

    @Test
    fun `execute returns Left ValidationFailed when dto has invalid id`() =
        runTest {
            val dto = sampleDto().copy(id = 0L)
            val error = useCase.execute(dto).shouldBeLeft()
            error shouldBe GitHubRepoSaveError.ValidationFailed(GitHubError.InvalidId(0L))
        }

    @Test
    fun `execute returns Left SaveFailed when repository returns error`() =
        runTest {
            val domainError = GitHubError.RepositoryError("Save failed")
            coEvery { repository.save(any()) } returns Either.Left(domainError)

            val error = useCase.execute(sampleDto()).shouldBeLeft()
            error shouldBe GitHubRepoSaveError.SaveFailed(domainError)
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

private fun sampleDto() =
    GitHubRepoDto(
        id = 1L,
        owner = "octocat",
        name = "hello-world",
        fullName = "octocat/hello-world",
        description = null,
        language = "Kotlin",
        stargazersCount = 0,
        forksCount = 0,
        isPrivate = false,
        createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
        updatedAt = OffsetDateTime.parse("2024-01-02T00:00:00Z"),
    )
