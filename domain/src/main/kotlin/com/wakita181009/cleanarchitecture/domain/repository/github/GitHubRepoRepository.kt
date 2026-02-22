package com.wakita181009.cleanarchitecture.domain.repository.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError

interface GitHubRepoRepository {
    suspend fun save(repo: GitHubRepo): Either<GitHubError, GitHubRepo>
}
