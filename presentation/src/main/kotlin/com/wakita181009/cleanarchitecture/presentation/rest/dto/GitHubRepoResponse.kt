package com.wakita181009.cleanarchitecture.presentation.rest.dto

import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import java.time.OffsetDateTime

data class GitHubRepoResponse(
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
    companion object {
        fun fromDomain(repo: GitHubRepo) =
            GitHubRepoResponse(
                id = repo.id.value,
                owner = repo.owner.value,
                name = repo.name.value,
                fullName = repo.fullName,
                description = repo.description,
                language = repo.language,
                stargazersCount = repo.stargazersCount,
                forksCount = repo.forksCount,
                isPrivate = repo.isPrivate,
                createdAt = repo.createdAt,
                updatedAt = repo.updatedAt,
            )

        fun fromQueryDto(dto: GitHubRepoQueryDto) =
            GitHubRepoResponse(
                id = dto.id,
                owner = dto.owner,
                name = dto.name,
                fullName = dto.fullName,
                description = dto.description,
                language = dto.language,
                stargazersCount = dto.stargazersCount,
                forksCount = dto.forksCount,
                isPrivate = dto.isPrivate,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
            )
    }
}
