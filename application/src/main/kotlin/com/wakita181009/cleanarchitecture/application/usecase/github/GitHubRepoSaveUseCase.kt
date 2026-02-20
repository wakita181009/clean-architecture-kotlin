package com.wakita181009.cleanarchitecture.application.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoSaveError
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo

interface GitHubRepoSaveUseCase {
    suspend fun execute(repo: GitHubRepo): Either<GitHubRepoSaveError, GitHubRepo>
}
