package com.wakita181009.cleanarchitecture.application.query.error.github

import com.wakita181009.cleanarchitecture.application.error.ApplicationError
import com.wakita181009.cleanarchitecture.domain.error.PageNumberError
import com.wakita181009.cleanarchitecture.domain.error.PageSizeError

sealed interface GitHubRepoListQueryError : ApplicationError {
    data class InvalidPageNumber(
        val error: PageNumberError,
    ) : GitHubRepoListQueryError {
        override val message = error.message
    }

    data class InvalidPageSize(
        val error: PageSizeError,
    ) : GitHubRepoListQueryError {
        override val message = error.message
    }

    data class FetchFailed(
        override val message: String,
    ) : GitHubRepoListQueryError
}
