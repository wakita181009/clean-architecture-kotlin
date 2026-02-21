package com.wakita181009.cleanarchitecture.presentation.graphql

import com.netflix.dgs.codegen.generated.types.GitHubRepo
import com.netflix.dgs.codegen.generated.types.GitHubRepoInput
import com.netflix.dgs.codegen.generated.types.GitHubRepoPage
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.wakita181009.cleanarchitecture.application.error.github.GitHubRepoFindByIdError
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoFindByIdUseCase
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoListUseCase
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoSaveUseCase
import graphql.GraphQLException

@DgsComponent
class GitHubRepoDataFetcher(
    private val gitHubRepoListUseCase: GitHubRepoListUseCase,
    private val gitHubRepoFindByIdUseCase: GitHubRepoFindByIdUseCase,
    private val gitHubRepoSaveUseCase: GitHubRepoSaveUseCase,
) {
    @DgsQuery(field = "githubRepo")
    suspend fun githubRepo(
        @InputArgument id: Long,
    ): GitHubRepo? =
        gitHubRepoFindByIdUseCase
            .execute(id)
            .fold(
                ifLeft = { error ->
                    when (error) {
                        is GitHubRepoFindByIdError.NotFound -> null
                        is GitHubRepoFindByIdError.InvalidId,
                        is GitHubRepoFindByIdError.FetchFailed,
                        -> throw GraphQLException(error.message)
                    }
                },
                ifRight = { repo -> repo.toGraphQL() },
            )

    @DgsQuery(field = "githubRepos")
    suspend fun githubRepos(
        @InputArgument pageNumber: Int = 1,
        @InputArgument pageSize: Int = 20,
    ): GitHubRepoPage =
        gitHubRepoListUseCase.execute(pageNumber, pageSize).fold(
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
