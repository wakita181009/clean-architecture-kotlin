package com.wakita181009.cleanarchitecture.presentation.graphql

import com.netflix.dgs.codegen.generated.types.GitHubRepo
import com.netflix.dgs.codegen.generated.types.GitHubRepoInput
import com.netflix.dgs.codegen.generated.types.GitHubRepoPage
import com.wakita181009.cleanarchitecture.application.dto.github.GitHubRepoDto
import com.wakita181009.cleanarchitecture.domain.valueobject.Page
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo as DomainGitHubRepo

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

fun Page<DomainGitHubRepo>.toGraphQL() =
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
