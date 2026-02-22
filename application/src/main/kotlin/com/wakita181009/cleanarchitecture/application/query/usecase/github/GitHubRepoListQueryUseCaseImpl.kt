package com.wakita181009.cleanarchitecture.application.query.usecase.github

import arrow.core.Either
import arrow.core.raise.either
import com.wakita181009.cleanarchitecture.application.query.dto.PageDto
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoListQueryError
import com.wakita181009.cleanarchitecture.application.query.repository.github.GitHubRepoQueryRepository
import com.wakita181009.cleanarchitecture.domain.valueobject.PageNumber
import com.wakita181009.cleanarchitecture.domain.valueobject.PageSize

class GitHubRepoListQueryUseCaseImpl(
    private val queryRepository: GitHubRepoQueryRepository,
) : GitHubRepoListQueryUseCase {
    override suspend fun execute(
        pageNumber: Int,
        pageSize: Int,
    ): Either<GitHubRepoListQueryError, PageDto<GitHubRepoQueryDto>> =
        either {
            val validPageNumber =
                PageNumber
                    .of(pageNumber)
                    .mapLeft(GitHubRepoListQueryError::InvalidPageNumber)
                    .bind()
            val validPageSize =
                PageSize
                    .of(pageSize)
                    .mapLeft(GitHubRepoListQueryError::InvalidPageSize)
                    .bind()

            val limit = validPageSize.value
            val offset = (validPageNumber.value - 1) * validPageSize.value
            queryRepository.list(limit, offset).bind()
        }
}
