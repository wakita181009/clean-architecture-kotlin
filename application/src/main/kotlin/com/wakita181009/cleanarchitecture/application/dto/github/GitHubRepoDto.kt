package com.wakita181009.cleanarchitecture.application.dto.github

import arrow.core.raise.either
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubOwner
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoName
import java.time.OffsetDateTime

data class GitHubRepoDto(
    val id: Long,
    val owner: String,
    val name: String,
    val fullName: String,
    val description: String?,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val isPrivate: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    fun toDomain() =
        either {
            GitHubRepo(
                id = GitHubRepoId.of(id).bind(),
                owner = GitHubOwner.of(owner).bind(),
                name = GitHubRepoName.of(name).bind(),
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
}
