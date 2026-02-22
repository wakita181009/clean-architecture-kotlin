package com.wakita181009.cleanarchitecture.presentation.rest

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.command.error.github.GitHubRepoSaveError
import com.wakita181009.cleanarchitecture.application.command.usecase.github.GitHubRepoSaveUseCase
import com.wakita181009.cleanarchitecture.application.query.dto.PageDto
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoFindByIdQueryError
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoListQueryError
import com.wakita181009.cleanarchitecture.application.query.usecase.github.GitHubRepoFindByIdQueryUseCase
import com.wakita181009.cleanarchitecture.application.query.usecase.github.GitHubRepoListQueryUseCase
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.error.PageNumberError
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubOwner
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoName
import com.wakita181009.cleanarchitecture.presentation.rest.dto.ErrorResponse
import com.wakita181009.cleanarchitecture.presentation.rest.dto.GitHubRepoListResponse
import com.wakita181009.cleanarchitecture.presentation.rest.dto.GitHubRepoRequest
import com.wakita181009.cleanarchitecture.presentation.rest.dto.GitHubRepoResponse
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime
import kotlin.test.Test

class GitHubRepoControllerTest {
    private val listQuery = mockk<GitHubRepoListQueryUseCase>()
    private val findByIdQuery = mockk<GitHubRepoFindByIdQueryUseCase>()
    private val saveUseCase = mockk<GitHubRepoSaveUseCase>()
    private val controller = GitHubRepoController(listQuery, findByIdQuery, saveUseCase)

    // --- list ---

    @Test
    fun `list returns 200 with page body when query succeeds`() =
        runTest {
            val page = PageDto(totalCount = 1, items = listOf(sampleQueryDto()))
            coEvery { listQuery.execute(1, 20) } returns Either.Right(page)

            val response = controller.list(1, 20)
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldBe GitHubRepoListResponse.fromQueryDtos(page)
        }

    @Test
    fun `list returns 400 with error body when query fails`() =
        runTest {
            val error = GitHubRepoListQueryError.InvalidPageNumber(PageNumberError.BelowMinimum(0))
            coEvery { listQuery.execute(0, 20) } returns Either.Left(error)

            val response = controller.list(0, 20)
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body shouldBe ErrorResponse(error.message)
        }

    // --- findById ---

    @Test
    fun `findById returns 200 with repo body when found`() =
        runTest {
            val dto = sampleQueryDto()
            coEvery { findByIdQuery.execute(1L) } returns Either.Right(dto)

            val response = controller.findById(1L)
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldBe GitHubRepoResponse.fromQueryDto(dto)
        }

    @Test
    fun `findById returns 400 when id is invalid`() =
        runTest {
            val error = GitHubRepoFindByIdQueryError.InvalidId(GitHubError.InvalidId(0L))
            coEvery { findByIdQuery.execute(0L) } returns Either.Left(error)

            val response = controller.findById(0L)
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body shouldBe ErrorResponse(error.message)
        }

    @Test
    fun `findById returns 404 when repo is not found`() =
        runTest {
            val error = GitHubRepoFindByIdQueryError.NotFound(1L)
            coEvery { findByIdQuery.execute(1L) } returns Either.Left(error)

            val response = controller.findById(1L)
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }

    @Test
    fun `findById returns 500 when fetch fails`() =
        runTest {
            val error = GitHubRepoFindByIdQueryError.FetchFailed("DB error")
            coEvery { findByIdQuery.execute(1L) } returns Either.Left(error)

            val response = controller.findById(1L)
            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
            response.body shouldBe ErrorResponse("Internal server error")
        }

    // --- save ---

    @Test
    fun `save returns 200 with repo body on success`() =
        runTest {
            val repo = sampleRepo()
            coEvery { saveUseCase.execute(any()) } returns Either.Right(repo)

            val response = controller.save(sampleRequest())
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldBe GitHubRepoResponse.fromDomain(repo)
        }

    @Test
    fun `save returns 422 with error body when validation fails`() =
        runTest {
            val error = GitHubRepoSaveError.ValidationFailed(GitHubError.InvalidId(0L))
            coEvery { saveUseCase.execute(any()) } returns Either.Left(error)

            val response = controller.save(sampleRequest())
            response.statusCode shouldBe HttpStatus.UNPROCESSABLE_CONTENT
            response.body shouldBe ErrorResponse(error.message)
        }

    @Test
    fun `save returns 500 with error body when save fails`() =
        runTest {
            val error = GitHubRepoSaveError.SaveFailed(GitHubError.RepositoryError("Save failed"))
            coEvery { saveUseCase.execute(any()) } returns Either.Left(error)

            val response = controller.save(sampleRequest())
            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
            response.body shouldBe ErrorResponse("Internal server error")
        }
}

private fun sampleQueryDto() =
    GitHubRepoQueryDto(
        id = 1L,
        owner = "octocat",
        name = "hello-world",
        fullName = "octocat/hello-world",
        description = "A test repo",
        language = "Kotlin",
        stargazersCount = 42,
        forksCount = 10,
        isPrivate = false,
        createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
        updatedAt = OffsetDateTime.parse("2024-01-02T00:00:00Z"),
    )

private fun sampleRepo() =
    GitHubRepo(
        id = GitHubRepoId(1L),
        owner = GitHubOwner("octocat"),
        name = GitHubRepoName("hello-world"),
        fullName = "octocat/hello-world",
        description = "A test repo",
        language = "Kotlin",
        stargazersCount = 42,
        forksCount = 10,
        isPrivate = false,
        createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
        updatedAt = OffsetDateTime.parse("2024-01-02T00:00:00Z"),
    )

private fun sampleRequest() =
    GitHubRepoRequest(
        id = 1L,
        owner = "octocat",
        name = "hello-world",
        fullName = "octocat/hello-world",
        description = "A test repo",
        language = "Kotlin",
        stargazersCount = 42,
        forksCount = 10,
        isPrivate = false,
        createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
        updatedAt = OffsetDateTime.parse("2024-01-02T00:00:00Z"),
    )
