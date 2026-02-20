package com.wakita181009.cleanarchitecture.infrastructure.repository.github

import com.wakita181009.cleanarchitecture.infrastructure.entity.GitHubRepoR2dbcEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface GitHubRepoR2dbcRepository : R2dbcRepository<GitHubRepoR2dbcEntity, Long> {
    @Query("SELECT * FROM github_repo ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun list(
        limit: Int,
        offset: Int,
    ): Flux<GitHubRepoR2dbcEntity>
}
