package com.wakita181009.cleanarchitecture.application.usecase.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoListError
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.valueobject.Page

interface GitHubRepoListUseCase {
    suspend fun execute(
        pageNumber: Int,
        pageSize: Int,
    ): Either<GitHubRepoListError, Page<GitHubRepo>>
}
