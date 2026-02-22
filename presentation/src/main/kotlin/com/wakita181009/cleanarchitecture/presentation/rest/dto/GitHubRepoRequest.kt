package com.wakita181009.cleanarchitecture.presentation.rest.dto

import com.wakita181009.cleanarchitecture.application.command.dto.github.GitHubRepoDto
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
    fun toDto() =
        GitHubRepoDto(
            id = id,
            owner = owner,
            name = name,
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
