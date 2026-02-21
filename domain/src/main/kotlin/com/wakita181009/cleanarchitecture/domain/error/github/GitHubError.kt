package com.wakita181009.cleanarchitecture.domain.error.github

import com.wakita181009.cleanarchitecture.domain.error.DomainError
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId

sealed interface GitHubError : DomainError {
    data class InvalidId(
        val value: Long,
    ) : GitHubError {
        override val message = "Invalid GitHub repo ID: $value (must be positive)"
    }

    data object InvalidName : GitHubError {
        override val message = "GitHub repo name must not be blank"
    }

    data object InvalidOwner : GitHubError {
        override val message = "GitHub owner must not be blank"
    }

    data class NotFound(
        val id: GitHubRepoId,
    ) : GitHubError {
        override val message = "GitHub repo not found: ${id.value}"
    }

    data class RepositoryError(
        override val message: String,
        val cause: Throwable? = null,
    ) : GitHubError
}
