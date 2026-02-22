package com.wakita181009.cleanarchitecture.application.command.error.github

import com.wakita181009.cleanarchitecture.application.error.ApplicationError
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError

sealed interface GitHubRepoSaveError : ApplicationError {
    data class ValidationFailed(
        val cause: GitHubError,
    ) : GitHubRepoSaveError {
        override val message = cause.message
    }

    data class SaveFailed(
        val cause: GitHubError,
    ) : GitHubRepoSaveError {
        override val message = cause.message
    }
}
