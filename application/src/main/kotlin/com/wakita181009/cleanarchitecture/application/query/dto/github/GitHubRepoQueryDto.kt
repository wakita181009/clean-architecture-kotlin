package com.wakita181009.cleanarchitecture.application.query.dto.github

import java.time.OffsetDateTime

data class GitHubRepoQueryDto(
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
)
