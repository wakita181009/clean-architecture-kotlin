package com.wakita181009.cleanarchitecture.presentation.rest.dto

import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubOwner
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoName
import java.time.OffsetDateTime

data class GitHubRepoRequest(
    val id: Long,
    val owner: String,
    val name: String,
    val fullName: String,
    val description: String? = null,
    val language: String? = null,
    val stargazersCount: Int = 0,
    val forksCount: Int = 0,
    val isPrivate: Boolean = false,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    fun toDomain() =
        GitHubRepo(
            id = GitHubRepoId(id),
            owner = GitHubOwner(owner),
            name = GitHubRepoName(name),
            fullName = fullName,
            description = description,
            language = language,
            stargazersCount = stargazersCount,
            forksCount = forksCount,
            isPrivate = isPrivate,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
