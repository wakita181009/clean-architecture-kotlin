package com.wakita181009.cleanarchitecture.application.error.github

import com.wakita181009.cleanarchitecture.application.error.ApplicationError
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError

sealed interface GitHubRepoFindByIdError : ApplicationError {
    data class NotFound(
        val cause: GitHubError,
    ) : GitHubRepoFindByIdError {
        override val message = cause.message
    }

    data class FetchFailed(
        val cause: GitHubError,
    ) : GitHubRepoFindByIdError {
        override val message = "Failed to fetch GitHub repo: ${cause.message}"
    }
}
