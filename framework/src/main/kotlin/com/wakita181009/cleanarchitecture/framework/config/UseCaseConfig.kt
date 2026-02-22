package com.wakita181009.cleanarchitecture.framework.config

import com.wakita181009.cleanarchitecture.application.command.usecase.github.GitHubRepoSaveUseCaseImpl
import com.wakita181009.cleanarchitecture.application.query.repository.github.GitHubRepoQueryRepository
import com.wakita181009.cleanarchitecture.application.query.usecase.github.GitHubRepoFindByIdQueryUseCaseImpl
import com.wakita181009.cleanarchitecture.application.query.usecase.github.GitHubRepoListQueryUseCaseImpl
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfig(
    private val gitHubRepoRepository: GitHubRepoRepository,
    private val gitHubRepoQueryRepository: GitHubRepoQueryRepository,
) {
    @Bean
    fun gitHubRepoFindByIdQuery() = GitHubRepoFindByIdQueryUseCaseImpl(gitHubRepoQueryRepository)

    @Bean
    fun gitHubRepoListQuery() = GitHubRepoListQueryUseCaseImpl(gitHubRepoQueryRepository)

    @Bean
    fun gitHubRepoSaveUseCase() = GitHubRepoSaveUseCaseImpl(gitHubRepoRepository)
}
