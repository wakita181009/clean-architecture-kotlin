package com.wakita181009.cleanarchitecture.application.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoFindByIdError
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo

interface GitHubRepoFindByIdUseCase {
    suspend fun execute(id: Long): Either<GitHubRepoFindByIdError, GitHubRepo>
}
