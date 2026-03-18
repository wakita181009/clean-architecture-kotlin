package com.wakita181009.cleanarchitecture.application.query.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoFindByIdQueryError

interface GitHubRepoFindByIdQueryUseCase {
    fun execute(id: Long): Either<GitHubRepoFindByIdQueryError, GitHubRepoQueryDto>
}
