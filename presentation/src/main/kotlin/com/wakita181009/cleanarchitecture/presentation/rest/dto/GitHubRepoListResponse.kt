package com.wakita181009.cleanarchitecture.presentation.rest.dto

import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.valueobject.Page

data class GitHubRepoListResponse(
    val items: List<GitHubRepoResponse>,
    val totalCount: Int,
) {
    companion object {
        fun fromDomain(page: Page<GitHubRepo>) =
            GitHubRepoListResponse(
                items = page.items.map(GitHubRepoResponse::fromDomain),
                totalCount = page.totalCount,
            )
    }
}
