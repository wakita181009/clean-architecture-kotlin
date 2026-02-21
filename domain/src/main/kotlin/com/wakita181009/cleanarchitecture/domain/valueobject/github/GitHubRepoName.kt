package com.wakita181009.cleanarchitecture.domain.valueobject.github

import arrow.core.raise.either
import arrow.core.raise.ensure
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError

@JvmInline
value class GitHubRepoName private constructor(
    val value: String,
) {
    companion object {
        operator fun invoke(value: String) = GitHubRepoName(value)

        fun of(value: String) =
            either {
                ensure(value.isNotBlank()) { GitHubError.InvalidName }
                GitHubRepoName(value)
            }
    }
}
