package com.wakita181009.cleanarchitecture.presentation.rest

import com.wakita181009.cleanarchitecture.application.command.error.github.GitHubRepoSaveError
import com.wakita181009.cleanarchitecture.application.command.usecase.github.GitHubRepoSaveUseCase
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoFindByIdQueryError
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoListQueryError
import com.wakita181009.cleanarchitecture.application.query.usecase.github.GitHubRepoFindByIdQueryUseCase
import com.wakita181009.cleanarchitecture.application.query.usecase.github.GitHubRepoListQueryUseCase
import com.wakita181009.cleanarchitecture.presentation.rest.dto.ErrorResponse
import com.wakita181009.cleanarchitecture.presentation.rest.dto.GitHubRepoListResponse
import com.wakita181009.cleanarchitecture.presentation.rest.dto.GitHubRepoRequest
import com.wakita181009.cleanarchitecture.presentation.rest.dto.GitHubRepoResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/github-repos")
class GitHubRepoController(
    private val gitHubRepoListQueryUseCase: GitHubRepoListQueryUseCase,
    private val gitHubRepoFindByIdQueryUseCase: GitHubRepoFindByIdQueryUseCase,
    private val gitHubRepoSaveUseCase: GitHubRepoSaveUseCase,
) {
    private val logger = LoggerFactory.getLogger(GitHubRepoController::class.java)

    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "1") pageNumber: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ResponseEntity<*> =
        gitHubRepoListQueryUseCase.execute(pageNumber, pageSize).fold(
            ifLeft = { error ->
                when (error) {
                    is GitHubRepoListQueryError.InvalidPageNumber,
                    is GitHubRepoListQueryError.InvalidPageSize,
                    -> ResponseEntity.badRequest().body(ErrorResponse(error.message))
                    is GitHubRepoListQueryError.FetchFailed -> {
                        logger.error("Failed to list GitHub repos: ${error.message}")
                        ResponseEntity.internalServerError().body(ErrorResponse("Internal server error"))
                    }
                }
            },
            ifRight = { page ->
                ResponseEntity.ok(GitHubRepoListResponse.fromQueryDtos(page))
            },
        )

    @GetMapping("/{id}")
    suspend fun findById(
        @PathVariable id: Long,
    ): ResponseEntity<*> =
        gitHubRepoFindByIdQueryUseCase.execute(id).fold(
            ifLeft = { error ->
                when (error) {
                    is GitHubRepoFindByIdQueryError.InvalidId ->
                        ResponseEntity.badRequest().body(ErrorResponse(error.message))
                    is GitHubRepoFindByIdQueryError.NotFound ->
                        ResponseEntity.notFound().build<Nothing>()
                    is GitHubRepoFindByIdQueryError.FetchFailed -> {
                        logger.error("Failed to fetch GitHub repo (id=$id): ${error.message}")
                        ResponseEntity.internalServerError().body(ErrorResponse("Internal server error"))
                    }
                }
            },
            ifRight = { dto ->
                ResponseEntity.ok(GitHubRepoResponse.fromQueryDto(dto))
            },
        )

    @PostMapping
    suspend fun save(
        @RequestBody request: GitHubRepoRequest,
    ): ResponseEntity<*> =
        gitHubRepoSaveUseCase.execute(request.toDto()).fold(
            ifLeft = { error ->
                when (error) {
                    is GitHubRepoSaveError.ValidationFailed ->
                        ResponseEntity.unprocessableContent().body(ErrorResponse(error.message))
                    is GitHubRepoSaveError.SaveFailed -> {
                        logger.error("Failed to save GitHub repo: ${error.message}")
                        ResponseEntity.internalServerError().body(ErrorResponse("Internal server error"))
                    }
                }
            },
            ifRight = { repo ->
                ResponseEntity.ok(GitHubRepoResponse.fromDomain(repo))
            },
        )
}
