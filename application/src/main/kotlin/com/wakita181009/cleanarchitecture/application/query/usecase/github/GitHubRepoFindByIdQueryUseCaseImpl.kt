package com.wakita181009.cleanarchitecture.application.query.usecase.github

import arrow.core.Either
import arrow.core.raise.either
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoFindByIdQueryError
import com.wakita181009.cleanarchitecture.application.query.repository.github.GitHubRepoQueryRepository
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId

class GitHubRepoFindByIdQueryUseCaseImpl(
    private val queryRepository: GitHubRepoQueryRepository,
) : GitHubRepoFindByIdQueryUseCase {
    override suspend fun execute(id: Long): Either<GitHubRepoFindByIdQueryError, GitHubRepoQueryDto> =
        either {
            val repoId = GitHubRepoId.of(id).mapLeft(GitHubRepoFindByIdQueryError::InvalidId).bind()
            queryRepository.findById(repoId.value).bind()
        }
}
