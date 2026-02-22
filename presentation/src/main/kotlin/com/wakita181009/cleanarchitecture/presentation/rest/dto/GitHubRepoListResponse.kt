package com.wakita181009.cleanarchitecture.presentation.rest.dto

import com.wakita181009.cleanarchitecture.application.query.dto.PageDto
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto

data class GitHubRepoListResponse(
    val items: List<GitHubRepoResponse>,
    val totalCount: Int,
) {
    companion object {
        fun fromQueryDtos(page: PageDto<GitHubRepoQueryDto>) =
            GitHubRepoListResponse(
                items = page.items.map(GitHubRepoResponse::fromQueryDto),
                totalCount = page.totalCount,
            )
    }
}
