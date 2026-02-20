package com.wakita181009.cleanarchitecture.domain.entity.github

import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubOwner
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoName
import java.time.OffsetDateTime

data class GitHubRepo(
    val id: GitHubRepoId,
    val owner: GitHubOwner,
    val name: GitHubRepoName,
    val fullName: String,
    val description: String?,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val isPrivate: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
