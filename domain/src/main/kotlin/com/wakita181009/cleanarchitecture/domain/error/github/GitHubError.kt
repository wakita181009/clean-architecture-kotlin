package com.wakita181009.cleanarchitecture.domain.error.github

import com.wakita181009.cleanarchitecture.domain.error.DomainError

sealed interface GitHubError : DomainError {
    data class InvalidId(
        val cause: Throwable? = null,
    ) : GitHubError {
        override val message = "Invalid GitHub repo ID format"
    }

    data class NotFound(
        val id: Long,
    ) : GitHubError {
        override val message = "GitHub repo not found: $id"
    }

    data class RepositoryError(
        override val message: String,
        val cause: Throwable? = null,
    ) : GitHubError
}
