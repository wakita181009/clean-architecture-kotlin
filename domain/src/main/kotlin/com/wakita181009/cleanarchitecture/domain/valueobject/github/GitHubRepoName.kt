package com.wakita181009.cleanarchitecture.domain.valueobject.github

@JvmInline
value class GitHubRepoName private constructor(
    val value: String,
) {
    companion object {
        operator fun invoke(value: String) = GitHubRepoName(value)
    }
}
