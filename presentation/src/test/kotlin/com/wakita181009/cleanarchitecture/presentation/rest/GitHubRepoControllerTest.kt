package com.wakita181009.cleanarchitecture.presentation.rest

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoFindByIdError
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoListError
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoSaveError
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoFindByIdUseCase
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoListUseCase
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoSaveUseCase
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.error.PageNumberError
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import com.wakita181009.cleanarchitecture.domain.valueobject.Page
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
    private val listUseCase = mockk<GitHubRepoListUseCase>()
    private val findByIdUseCase = mockk<GitHubRepoFindByIdUseCase>()
    private val saveUseCase = mockk<GitHubRepoSaveUseCase>()
    private val controller = GitHubRepoController(listUseCase, findByIdUseCase, saveUseCase)

    // --- list ---

    @Test
    fun `list returns 200 with page body when use case succeeds`() =
        runTest {
            val page = Page(totalCount = 1, items = listOf(sampleRepo()))
            coEvery { listUseCase.execute(1, 20) } returns Either.Right(page)

            val response = controller.list(1, 20)
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldBe GitHubRepoListResponse.fromDomain(page)
        }

    @Test
    fun `list returns 400 with error body when use case fails`() =
        runTest {
            val error = GitHubRepoListError.InvalidPageNumber(PageNumberError.BelowMinimum(0))
            coEvery { listUseCase.execute(0, 20) } returns Either.Left(error)

            val response = controller.list(0, 20)
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body shouldBe ErrorResponse(error.message)
        }

    // --- findById ---

    @Test
    fun `findById returns 200 with repo body when found`() =
        runTest {
            val repo = sampleRepo()
            coEvery { findByIdUseCase.execute(1L) } returns Either.Right(repo)

            val response = controller.findById(1L)
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldBe GitHubRepoResponse.fromDomain(repo)
        }

    @Test
    fun `findById returns 400 when id is invalid`() =
        runTest {
            val error = GitHubRepoFindByIdError.InvalidId(GitHubError.InvalidId(0L))
            coEvery { findByIdUseCase.execute(0L) } returns Either.Left(error)

            val response = controller.findById(0L)
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body shouldBe ErrorResponse(error.message)
        }

    @Test
    fun `findById returns 404 when repo is not found`() =
        runTest {
            val error = GitHubRepoFindByIdError.NotFound(GitHubError.NotFound(GitHubRepoId(1L)))
            coEvery { findByIdUseCase.execute(1L) } returns Either.Left(error)

            val response = controller.findById(1L)
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }

    @Test
    fun `findById returns 500 when fetch fails`() =
        runTest {
            val error = GitHubRepoFindByIdError.FetchFailed(GitHubError.RepositoryError("DB error"))
            coEvery { findByIdUseCase.execute(1L) } returns Either.Left(error)

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
