package com.wakita181009.cleanarchitecture.presentation.rest

import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoFindByIdError
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoFindByIdUseCase
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoListUseCase
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoSaveUseCase
import com.wakita181009.cleanarchitecture.presentation.rest.dto.ErrorResponse
import com.wakita181009.cleanarchitecture.presentation.rest.dto.GitHubRepoListResponse
import com.wakita181009.cleanarchitecture.presentation.rest.dto.GitHubRepoRequest
import com.wakita181009.cleanarchitecture.presentation.rest.dto.GitHubRepoResponse
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
    private val gitHubRepoListUseCase: GitHubRepoListUseCase,
    private val gitHubRepoFindByIdUseCase: GitHubRepoFindByIdUseCase,
    private val gitHubRepoSaveUseCase: GitHubRepoSaveUseCase,
) {
    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "1") pageNumber: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ResponseEntity<*> =
        gitHubRepoListUseCase.execute(pageNumber, pageSize).fold(
            ifLeft = { error ->
                ResponseEntity.badRequest().body(ErrorResponse(error.message))
            },
            ifRight = { page ->
                ResponseEntity.ok(GitHubRepoListResponse.fromDomain(page))
            },
        )

    @GetMapping("/{id}")
    suspend fun findById(
        @PathVariable id: Long,
    ): ResponseEntity<*> =
        gitHubRepoFindByIdUseCase.execute(id).fold(
            ifLeft = { error ->
                when (error) {
                    is GitHubRepoFindByIdError.NotFound ->
                        ResponseEntity.notFound().build<Nothing>()
                    is GitHubRepoFindByIdError.FetchFailed ->
                        ResponseEntity.badRequest().body(ErrorResponse(error.message))
                }
            },
            ifRight = { repo ->
                ResponseEntity.ok(GitHubRepoResponse.fromDomain(repo))
            },
        )

    @PostMapping
    suspend fun save(
        @RequestBody request: GitHubRepoRequest,
    ): ResponseEntity<*> =
        gitHubRepoSaveUseCase.execute(request.toDomain()).fold(
            ifLeft = { error ->
                ResponseEntity.badRequest().body(ErrorResponse(error.message))
            },
            ifRight = { repo ->
                ResponseEntity.ok(GitHubRepoResponse.fromDomain(repo))
            },
        )
}
