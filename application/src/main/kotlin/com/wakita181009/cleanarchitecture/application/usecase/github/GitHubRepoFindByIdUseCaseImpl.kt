package com.wakita181009.cleanarchitecture.application.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoFindByIdError
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId

class GitHubRepoFindByIdUseCaseImpl(
    private val gitHubRepoRepository: GitHubRepoRepository,
) : GitHubRepoFindByIdUseCase {
    override suspend fun execute(id: Long): Either<GitHubRepoFindByIdError, GitHubRepo> =
        gitHubRepoRepository
            .findById(GitHubRepoId(id))
            .mapLeft { error ->
                when (error) {
                    is GitHubError.NotFound -> GitHubRepoFindByIdError.NotFound(error)
                    is GitHubError.InvalidId,
                    is GitHubError.RepositoryError,
                    -> GitHubRepoFindByIdError.FetchFailed(error)
                }
            }
}
