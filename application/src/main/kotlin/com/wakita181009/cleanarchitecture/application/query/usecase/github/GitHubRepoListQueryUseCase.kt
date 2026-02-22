package com.wakita181009.cleanarchitecture.application.query.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.query.dto.PageDto
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoListQueryError

interface GitHubRepoListQueryUseCase {
    suspend fun execute(
        pageNumber: Int,
        pageSize: Int,
    ): Either<GitHubRepoListQueryError, PageDto<GitHubRepoQueryDto>>
}
