package com.wakita181009.cleanarchitecture.application.usecase.github

import arrow.core.Either
import arrow.core.raise.either
import com.wakita181009.cleanarchitecture.application.dto.github.GitHubRepoDto
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoSaveError
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository

class GitHubRepoSaveUseCaseImpl(
    private val gitHubRepoRepository: GitHubRepoRepository,
) : GitHubRepoSaveUseCase {
    override suspend fun execute(dto: GitHubRepoDto): Either<GitHubRepoSaveError, GitHubRepo> =
        either {
            val repo = dto.toDomain().mapLeft(GitHubRepoSaveError::ValidationFailed).bind()
            gitHubRepoRepository
                .save(repo)
                .mapLeft(GitHubRepoSaveError::SaveFailed)
                .bind()
        }
}
