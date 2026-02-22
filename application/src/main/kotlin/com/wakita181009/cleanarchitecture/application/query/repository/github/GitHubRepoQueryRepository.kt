package com.wakita181009.cleanarchitecture.application.query.repository.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.query.dto.PageDto
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoFindByIdQueryError
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoListQueryError

interface GitHubRepoQueryRepository {
    suspend fun findById(id: Long): Either<GitHubRepoFindByIdQueryError, GitHubRepoQueryDto>

    suspend fun list(
        limit: Int,
        offset: Int,
    ): Either<GitHubRepoListQueryError, PageDto<GitHubRepoQueryDto>>
}
