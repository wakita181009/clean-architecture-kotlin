package com.wakita181009.cleanarchitecture.application.usecase.github

import arrow.core.Either
import arrow.core.raise.either
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoListError
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository
import com.wakita181009.cleanarchitecture.domain.valueobject.Page
import com.wakita181009.cleanarchitecture.domain.valueobject.PageNumber
import com.wakita181009.cleanarchitecture.domain.valueobject.PageSize

class GitHubRepoListUseCaseImpl(
    private val gitHubRepoRepository: GitHubRepoRepository,
) : GitHubRepoListUseCase {
    override suspend fun execute(
        pageNumber: Int,
        pageSize: Int,
    ): Either<GitHubRepoListError, Page<GitHubRepo>> =
        either {
            val validPageNumber =
                PageNumber
                    .of(pageNumber)
                    .mapLeft(GitHubRepoListError::InvalidPageNumber)
                    .bind()
            val validPageSize =
                PageSize
                    .of(pageSize)
                    .mapLeft(GitHubRepoListError::InvalidPageSize)
                    .bind()

            gitHubRepoRepository
                .list(validPageNumber, validPageSize)
                .mapLeft(GitHubRepoListError::FetchFailed)
                .bind()
        }
}
