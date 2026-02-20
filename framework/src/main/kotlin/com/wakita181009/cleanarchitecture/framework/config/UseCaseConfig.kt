package com.wakita181009.cleanarchitecture.framework.config

import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoFindByIdUseCaseImpl
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoListUseCaseImpl
import com.wakita181009.cleanarchitecture.application.usecase.github.GitHubRepoSaveUseCaseImpl
import com.wakita181009.cleanarchitecture.domain.repository.github.GitHubRepoRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfig(
    private val gitHubRepoRepository: GitHubRepoRepository,
) {
    @Bean
    fun gitHubRepoListUseCase() = GitHubRepoListUseCaseImpl(gitHubRepoRepository)

    @Bean
    fun gitHubRepoFindByIdUseCase() = GitHubRepoFindByIdUseCaseImpl(gitHubRepoRepository)

    @Bean
    fun gitHubRepoSaveUseCase() = GitHubRepoSaveUseCaseImpl(gitHubRepoRepository)
}
