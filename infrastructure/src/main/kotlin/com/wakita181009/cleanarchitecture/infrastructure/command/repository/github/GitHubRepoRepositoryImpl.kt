package com.wakita181009.cleanarchitecture.infrastructure.command.repository.github

import arrow.core.Either
import com.wakita181009.cleanarchitecture.domain.entity.github.GitHubRepo
import com.wakita181009.cleanarchitecture.domain.error.github.GitHubError
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubOwner
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoId
import com.wakita181009.cleanarchitecture.domain.valueobject.github.GitHubRepoName
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.generated.tables.records.GithubRepoRecord
import org.jooq.generated.tables.references.GITHUB_REPO
import org.jooq.impl.DSL.excluded
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class GitHubRepoRepositoryImpl(
    private val dsl: DSLContext,
) : GitHubRepoRepository {
    companion object {
        private fun GithubRepoRecord.toDomain() =
            GitHubRepo(
                id = GitHubRepoId(id!!),
                owner = GitHubOwner(owner!!),
                name = GitHubRepoName(name!!),
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

    override suspend fun save(repo: GitHubRepo): Either<GitHubError, GitHubRepo> =
        Either
            .catch {
                Mono
                    .from(
                        dsl
                            .insertInto(GITHUB_REPO)
                            .set(GITHUB_REPO.ID, repo.id.value)
                            .set(GITHUB_REPO.OWNER, repo.owner.value)
                            .set(GITHUB_REPO.NAME, repo.name.value)
                            .set(GITHUB_REPO.FULL_NAME, repo.fullName)
                            .set(GITHUB_REPO.DESCRIPTION, repo.description)
                            .set(GITHUB_REPO.LANGUAGE, repo.language)
                            .set(GITHUB_REPO.STARGAZERS_COUNT, repo.stargazersCount)
                            .set(GITHUB_REPO.FORKS_COUNT, repo.forksCount)
                            .set(GITHUB_REPO.IS_PRIVATE, repo.isPrivate)
                            .set(GITHUB_REPO.CREATED_AT, repo.createdAt)
                            .set(GITHUB_REPO.UPDATED_AT, repo.updatedAt)
                            .onConflict(GITHUB_REPO.ID)
                            .doUpdate()
                            .set(GITHUB_REPO.OWNER, excluded(GITHUB_REPO.OWNER))
                            .set(GITHUB_REPO.NAME, excluded(GITHUB_REPO.NAME))
                            .set(GITHUB_REPO.FULL_NAME, excluded(GITHUB_REPO.FULL_NAME))
                            .set(GITHUB_REPO.DESCRIPTION, excluded(GITHUB_REPO.DESCRIPTION))
                            .set(GITHUB_REPO.LANGUAGE, excluded(GITHUB_REPO.LANGUAGE))
                            .set(GITHUB_REPO.STARGAZERS_COUNT, excluded(GITHUB_REPO.STARGAZERS_COUNT))
                            .set(GITHUB_REPO.FORKS_COUNT, excluded(GITHUB_REPO.FORKS_COUNT))
                            .set(GITHUB_REPO.IS_PRIVATE, excluded(GITHUB_REPO.IS_PRIVATE))
                            .set(GITHUB_REPO.UPDATED_AT, excluded(GITHUB_REPO.UPDATED_AT))
                            .returning(),
                    ).map { it.toDomain() }
                    .awaitSingle()
            }.mapLeft { GitHubError.RepositoryError("Failed to save repo: ${it.message}", it) }
}
