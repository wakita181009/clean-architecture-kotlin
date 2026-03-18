package com.wakita181009.cleanarchitecture.infrastructure.query.repository.github

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.wakita181009.cleanarchitecture.application.query.dto.PageDto
import com.wakita181009.cleanarchitecture.application.query.dto.github.GitHubRepoQueryDto
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoFindByIdQueryError
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoListQueryError
import com.wakita181009.cleanarchitecture.application.query.repository.github.GitHubRepoQueryRepository
import org.jooq.DSLContext
import org.jooq.generated.tables.records.GithubRepoRecord
import org.jooq.generated.tables.references.GITHUB_REPO
import org.springframework.stereotype.Repository

@Repository
class GitHubRepoQueryRepositoryImpl(
    private val dsl: DSLContext,
) : GitHubRepoQueryRepository {
    companion object {
        private fun GithubRepoRecord.toQueryDto() =
            GitHubRepoQueryDto(
                id = id!!,
                owner = owner!!,
                name = name!!,
                fullName = fullName!!,
                description = description,
                language = language,
                stargazersCount = stargazersCount!!,
                forksCount = forksCount!!,
                isPrivate = isPrivate!!,
                createdAt = createdAt!!,
                updatedAt = updatedAt!!,
            )
    }

    override fun findById(id: Long): Either<GitHubRepoFindByIdQueryError, GitHubRepoQueryDto> =
        either {
            val dto =
                Either
                    .catch {
                        dsl
                            .selectFrom(GITHUB_REPO)
                            .where(GITHUB_REPO.ID.eq(id))
                            .fetchOne()
                            ?.toQueryDto()
                    }.mapLeft { GitHubRepoFindByIdQueryError.FetchFailed(it.message ?: "Unknown error") }
                    .bind()

            ensureNotNull(dto) { GitHubRepoFindByIdQueryError.NotFound(id) }
        }

    override fun list(
        limit: Int,
        offset: Int,
    ): Either<GitHubRepoListQueryError, PageDto<GitHubRepoQueryDto>> =
        Either
            .catch {
                val items =
                    dsl
                        .selectFrom(GITHUB_REPO)
                        .orderBy(GITHUB_REPO.CREATED_AT.desc())
                        .limit(limit)
                        .offset(offset)
                        .fetch()
                        .map { it.toQueryDto() }
                val totalCount =
                    dsl
                        .selectCount()
                        .from(GITHUB_REPO)
                        .fetchOne()!!
                        .value1()

                PageDto(totalCount, items)
            }.mapLeft { GitHubRepoListQueryError.FetchFailed(it.message ?: "Unknown error") }
}
