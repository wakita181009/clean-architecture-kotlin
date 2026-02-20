package com.wakita181009.cleanarchitecture.domain.repository.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import com.wakita181009.cleanarchitecture.domain.valueobject.Page
import com.wakita181009.cleanarchitecture.domain.valueobject.PageNumber
import com.wakita181009.cleanarchitecture.domain.valueobject.PageSize
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId

interface GitHubRepoRepository {
    suspend fun findById(id: GitHubRepoId): Either<GitHubError, GitHubRepo>

    suspend fun list(
        pageNumber: PageNumber,
        pageSize: PageSize,
    ): Either<GitHubError, Page<GitHubRepo>>

    suspend fun save(repo: GitHubRepo): Either<GitHubError, GitHubRepo>
}
