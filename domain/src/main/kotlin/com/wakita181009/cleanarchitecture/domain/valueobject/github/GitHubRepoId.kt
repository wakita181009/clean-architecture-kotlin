package com.wakita181009.cleanarchitecture.domain.valueobject.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError

@JvmInline
value class GitHubRepoId private constructor(
    val value: Long,
) {
    companion object {
        operator fun invoke(value: Long) = GitHubRepoId(value)

        fun of(value: String) =
            Either
                .catch {
                    GitHubRepoId(value.toLong())
                }.mapLeft { e ->
                    GitHubError.InvalidId(e)
                }
    }
}
