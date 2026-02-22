package com.wakita181009.cleanarchitecture.application.query.error.github

import com.wakita181009.cleanarchitecture.application.error.ApplicationError
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError

sealed interface GitHubRepoFindByIdQueryError : ApplicationError {
    data class InvalidId(
        val cause: GitHubError,
    ) : GitHubRepoFindByIdQueryError {
        override val message = cause.message
    }

    data class NotFound(
        val id: Long,
    ) : GitHubRepoFindByIdQueryError {
        override val message = "GitHub repo not found: $id"
    }

    data class FetchFailed(
        override val message: String,
    ) : GitHubRepoFindByIdQueryError
}
