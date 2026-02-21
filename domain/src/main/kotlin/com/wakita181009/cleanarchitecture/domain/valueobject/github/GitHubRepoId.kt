package com.wakita181009.cleanarchitecture.domain.valueobject.github

import arrow.core.raise.either
import arrow.core.raise.ensure
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError

@JvmInline
value class GitHubRepoId private constructor(
    val value: Long,
) {
    companion object {
        operator fun invoke(value: Long) = GitHubRepoId(value)

        fun of(value: Long) =
            either {
                ensure(value > 0L) { GitHubError.InvalidId(value) }
                GitHubRepoId(value)
            }
    }
}
