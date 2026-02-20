package com.wakita181009.cleanarchitecture.infrastructure.entity

import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubOwner
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoName
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("github_repo")
data class GitHubRepoR2dbcEntity(
    @Id
    val id: Long,
    val owner: String,
    val name: String,
    @Column("full_name")
    val fullName: String,
    val description: String?,
    val language: String?,
    @Column("stargazers_count")
    val stargazersCount: Int,
    @Column("forks_count")
    val forksCount: Int,
    @Column("is_private")
    val isPrivate: Boolean,
    @Column("created_at")
    val createdAt: OffsetDateTime,
    @Column("updated_at")
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

fun GitHubRepo.toEntity() =
    GitHubRepoR2dbcEntity(
        id = id.value,
        owner = owner.value,
        name = name.value,
        fullName = fullName,
        description = description,
        language = language,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        isPrivate = isPrivate,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
