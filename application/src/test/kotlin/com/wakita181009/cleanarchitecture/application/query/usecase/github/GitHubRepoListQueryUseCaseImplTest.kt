package com.wakita181009.cleanarchitecture.application.query.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.query.dto.PageDto
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoListQueryError
import com.wakita181009.cleanarchitecture.application.query.repository.github.GitHubRepoQueryRepository
import com.wakita181009.cleanarchitecture.domain.error.PageNumberError
import com.wakita181009.cleanarchitecture.domain.error.PageSizeError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.OffsetDateTime
import kotlin.test.Test

class GitHubRepoListQueryUseCaseImplTest {
    private val queryRepository = mockk<GitHubRepoQueryRepository>()
    private val query = GitHubRepoListQueryUseCaseImpl(queryRepository)

    @Test
    fun `execute returns Right with page when parameters are valid`() =
        runTest {
            val page = PageDto(totalCount = 1, items = listOf(sampleQueryDto()))
            coEvery { queryRepository.list(offset = 0, limit = 20) } returns Either.Right(page)

            query.execute(1, 20).shouldBeRight(page)
        }

    @Test
    fun `execute returns Right with empty page`() =
        runTest {
            val emptyPage = PageDto<GitHubRepoQueryDto>(totalCount = 0, items = emptyList())
            coEvery { queryRepository.list(offset = 0, limit = 20) } returns Either.Right(emptyPage)

            query.execute(1, 20).shouldBeRight(emptyPage)
        }

    @Test
    fun `execute returns Left InvalidPageNumber when pageNumber is 0`() =
        runTest {
            val error = query.execute(0, 20).shouldBeLeft()
            error shouldBe GitHubRepoListQueryError.InvalidPageNumber(PageNumberError.BelowMinimum(0))
        }

    @Test
    fun `execute returns Left InvalidPageSize when pageSize is 0`() =
        runTest {
            val error = query.execute(1, 0).shouldBeLeft()
            error shouldBe GitHubRepoListQueryError.InvalidPageSize(PageSizeError.BelowMinimum(0))
        }

    @Test
    fun `execute returns Left InvalidPageSize when pageSize exceeds 100`() =
        runTest {
            val error = query.execute(1, 101).shouldBeLeft()
            error shouldBe GitHubRepoListQueryError.InvalidPageSize(PageSizeError.AboveMaximum(101))
        }

    @Test
    fun `execute returns Left FetchFailed when repository returns error`() =
        runTest {
            val fetchError = GitHubRepoListQueryError.FetchFailed("DB error")
            coEvery { queryRepository.list(offset = 0, limit = 20) } returns Either.Left(fetchError)

            val error = query.execute(1, 20).shouldBeLeft()
            error shouldBe fetchError
        }

    @Test
    fun `execute calculates correct offset for page 2`() =
        runTest {
            val page = PageDto(totalCount = 30, items = listOf(sampleQueryDto()))
            coEvery { queryRepository.list(offset = 20, limit = 20) } returns Either.Right(page)

            query.execute(2, 20).shouldBeRight(page)
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
