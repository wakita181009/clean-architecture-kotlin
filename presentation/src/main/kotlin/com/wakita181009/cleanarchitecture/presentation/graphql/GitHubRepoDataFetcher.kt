package com.wakita181009.cleanarchitecture.presentation.graphql

import com.netflix.dgs.codegen.generated.types.GitHubRepo
import com.netflix.dgs.codegen.generated.types.GitHubRepoInput
import com.netflix.dgs.codegen.generated.types.GitHubRepoPage
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.wakita181009.cleanarchitecture.application.command.usecase.github.GitHubRepoSaveUseCase
import com.wakita181009.cleanarchitecture.application.query.error.github.GitHubRepoFindByIdQueryError
import com.wakita181009.cleanarchitecture.application.query.usecase.github.GitHubRepoFindByIdQueryUseCase
import com.wakita181009.cleanarchitecture.application.query.usecase.github.GitHubRepoListQueryUseCase
import graphql.GraphQLException

@DgsComponent
class GitHubRepoDataFetcher(
    private val gitHubRepoListQuery: GitHubRepoListQueryUseCase,
    private val gitHubRepoFindByIdQuery: GitHubRepoFindByIdQueryUseCase,
    private val gitHubRepoSaveUseCase: GitHubRepoSaveUseCase,
) {
    @DgsQuery(field = "githubRepo")
    suspend fun githubRepo(
        @InputArgument id: Long,
    ): GitHubRepo? =
        gitHubRepoFindByIdQuery
            .execute(id)
            .fold(
                ifLeft = { error ->
                    when (error) {
                        is GitHubRepoFindByIdQueryError.NotFound -> null
                        is GitHubRepoFindByIdQueryError.InvalidId,
                        is GitHubRepoFindByIdQueryError.FetchFailed,
                        -> throw GraphQLException(error.message)
                    }
                },
                ifRight = { dto -> dto.toGraphQL() },
            )

    @DgsQuery(field = "githubRepos")
    suspend fun githubRepos(
        @InputArgument pageNumber: Int = 1,
        @InputArgument pageSize: Int = 20,
    ): GitHubRepoPage =
        gitHubRepoListQuery.execute(pageNumber, pageSize).fold(
            ifLeft = { error -> throw GraphQLException(error.message) },
            ifRight = { page -> page.toGraphQL() },
        )

    @DgsMutation(field = "saveGitHubRepo")
    suspend fun saveGitHubRepo(
        @InputArgument input: GitHubRepoInput,
    ): GitHubRepo =
        gitHubRepoSaveUseCase.execute(input.toDto()).fold(
            ifLeft = { error -> throw GraphQLException(error.message) },
            ifRight = { repo -> repo.toGraphQL() },
        )
}
