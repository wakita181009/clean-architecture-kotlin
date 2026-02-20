package com.wakita181009.cleanarchitecture.application.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoSaveError
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository

class GitHubRepoSaveUseCaseImpl(
    private val gitHubRepoRepository: GitHubRepoRepository,
) : GitHubRepoSaveUseCase {
    override suspend fun execute(repo: GitHubRepo): Either<GitHubRepoSaveError, GitHubRepo> =
        gitHubRepoRepository
            .save(repo)
            .mapLeft(GitHubRepoSaveError::SaveFailed)
}
