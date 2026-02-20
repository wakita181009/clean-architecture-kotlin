package com.wakita181009.cleanarchitecture.infrastructure.repository.github

import arrow.core.Either
import arrow.core.raise.either
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository
import com.wakita181009.cleanarchitecture.domain.valueobject.Page
import com.wakita181009.cleanarchitecture.domain.valueobject.PageNumber
import com.wakita181009.cleanarchitecture.domain.valueobject.PageSize
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId
import com.wakita181009.cleanarchitecture.infrastructure.entity.toEntity
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Repository

@Repository
class GitHubRepoRepositoryImpl(
    private val r2dbcRepository: GitHubRepoR2dbcRepository,
) : GitHubRepoRepository {
    override suspend fun findById(id: GitHubRepoId): Either<GitHubError, GitHubRepo> =
        either {
            val entity =
                Either
                    .catch { r2dbcRepository.findById(id.value).awaitSingleOrNull() }
                    .mapLeft { GitHubError.RepositoryError("Failed to find repo: ${it.message}", it) }
                    .bind()
            if (entity == null) raise(GitHubError.NotFound(id.value))
            entity.toDomain()
        }

    override suspend fun list(
        pageNumber: PageNumber,
        pageSize: PageSize,
    ): Either<GitHubError, Page<GitHubRepo>> =
        Either
            .catch {
                val offset = (pageNumber.value - 1) * pageSize.value
                val items =
                    r2dbcRepository
                        .list(pageSize.value, offset)
                        .asFlow()
                        .map { it.toDomain() }
                        .toList()
                val total = r2dbcRepository.count().awaitSingle()
                Page(totalCount = total.toInt(), items = items)
            }.mapLeft { GitHubError.RepositoryError("Failed to list repos: ${it.message}", it) }

    override suspend fun save(repo: GitHubRepo): Either<GitHubError, GitHubRepo> =
        Either
            .catch { r2dbcRepository.save(repo.toEntity()).awaitSingle().toDomain() }
            .mapLeft { GitHubError.RepositoryError("Failed to save repo: ${it.message}", it) }
}
