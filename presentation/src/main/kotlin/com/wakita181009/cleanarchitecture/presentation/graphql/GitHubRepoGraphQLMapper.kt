package com.wakita181009.cleanarchitecture.presentation.graphql

import com.netflix.dgs.codegen.generated.types.GitHubRepo
import com.netflix.dgs.codegen.generated.types.GitHubRepoInput
import com.netflix.dgs.codegen.generated.types.GitHubRepoPage
import com.wakita181009.cleanarchitecture.application.command.dto.github.GitHubRepoDto
import com.wakita181009.cleanarchitecture.application.query.dto.PageDto
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo as DomainGitHubRepo

fun GitHubRepoQueryDto.toGraphQL() =
    GitHubRepo(
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

fun PageDto<GitHubRepoQueryDto>.toGraphQL() =
    GitHubRepoPage(
        items = items.map { it.toGraphQL() },
        totalCount = totalCount,
    )

fun GitHubRepoInput.toDto() =
    GitHubRepoDto(
        id = id,
        owner = owner,
        name = name,
        fullName = fullName,
        description = description,
        language = language,
        stargazersCount = stargazersCount ?: 0,
        forksCount = forksCount ?: 0,
        isPrivate = isPrivate ?: false,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun DomainGitHubRepo.toGraphQL() =
    GitHubRepo(
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
