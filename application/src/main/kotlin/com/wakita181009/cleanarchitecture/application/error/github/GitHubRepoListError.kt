package com.wakita181009.cleanarchitecture.application.error.github

import com.wakita181009.cleanarchitecture.application.error.ApplicationError
import com.wakita181009.cleanarchitecture.domain.error.PageNumberError
import com.wakita181009.cleanarchitecture.domain.error.PageSizeError
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError

sealed interface GitHubRepoListError : ApplicationError {
    data class InvalidPageNumber(
        val error: PageNumberError,
    ) : GitHubRepoListError {
        override val message = error.message
    }

    data class InvalidPageSize(
        val error: PageSizeError,
    ) : GitHubRepoListError {
        override val message = error.message
    }

    data class FetchFailed(
        val cause: GitHubError,
    ) : GitHubRepoListError {
        override val message = cause.message
    }
}
