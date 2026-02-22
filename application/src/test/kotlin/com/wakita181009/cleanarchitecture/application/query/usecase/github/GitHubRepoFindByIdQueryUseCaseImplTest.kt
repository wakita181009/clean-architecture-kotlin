package com.wakita181009.cleanarchitecture.application.query.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoFindByIdQueryError
import com.wakita181009.cleanarchitecture.application.query.repository.github.GitHubRepoQueryRepository
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.OffsetDateTime
import kotlin.test.Test

class GitHubRepoFindByIdQueryUseCaseImplTest {
    private val queryRepository = mockk<GitHubRepoQueryRepository>()
    private val query = GitHubRepoFindByIdQueryUseCaseImpl(queryRepository)

    @Test
    fun `execute returns Right with dto when found`() =
        runTest {
            val dto = sampleQueryDto()
            coEvery { queryRepository.findById(1L) } returns Either.Right(dto)

            query.execute(1L).shouldBeRight(dto)
        }

    @Test
    fun `execute returns Left InvalidId when id is not positive`() =
        runTest {
            val error = query.execute(0L).shouldBeLeft()
            error shouldBe GitHubRepoFindByIdQueryError.InvalidId(GitHubError.InvalidId(0L))
        }

    @Test
    fun `execute returns Left NotFound when repository returns NotFound`() =
        runTest {
            coEvery { queryRepository.findById(1L) } returns Either.Left(GitHubRepoFindByIdQueryError.NotFound(1L))

            val error = query.execute(1L).shouldBeLeft()
            error shouldBe GitHubRepoFindByIdQueryError.NotFound(1L)
        }

    @Test
    fun `execute returns Left FetchFailed when repository returns error`() =
        runTest {
            val fetchError = GitHubRepoFindByIdQueryError.FetchFailed("DB error")
            coEvery { queryRepository.findById(1L) } returns Either.Left(fetchError)

            val error = query.execute(1L).shouldBeLeft()
            error shouldBe fetchError
        }
}

private fun sampleQueryDto() =
    GitHubRepoQueryDto(
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
