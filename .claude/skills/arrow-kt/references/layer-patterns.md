# Layer-by-Layer Arrow-kt Patterns

## Domain Layer

### Value object with validation (`ensure`)

```kotlin
@JvmInline
value class PageNumber private constructor(val value: Int) {
    companion object {
        private const val MIN_VALUE = 1

        operator fun invoke(value: Int) = PageNumber(value)

        fun of(value: Int): Either<PageNumberError, PageNumber> =
            either {
                ensure(value >= MIN_VALUE) { PageNumberError.BelowMinimum(value) }
                PageNumber(value)
            }
    }
}
```

### Value object with exception catching (`Either.catch`)

```kotlin
@JvmInline
value class GitHubRepoId private constructor(val value: Long) {
    companion object {
        fun of(value: String): Either<GitHubError, GitHubRepoId> =
            Either
                .catch { GitHubRepoId(value.toLong()) }
                .mapLeft { e -> GitHubError.InvalidId(e) }
    }
}
```

### Repository interface

```kotlin
interface GitHubRepoRepository {
    fun findById(id: GitHubRepoId): Either<GitHubError, GitHubRepo>
    fun list(pageNumber: PageNumber, pageSize: PageSize): Either<GitHubError, Page<GitHubRepo>>
    fun save(repo: GitHubRepo): Either<GitHubError, GitHubRepo>
}
```

### Domain error sealed class

```kotlin
sealed class GitHubError {
    data class NotFound(val id: Long) : GitHubError()
    data class InvalidId(val cause: Throwable) : GitHubError()
    data class DatabaseError(val message: String, val cause: Throwable) : GitHubError()
}
```

---

## Application Layer

### Simple use case (single repository call)

```kotlin
class GitHubRepoFindByIdUseCaseImpl(
    private val gitHubRepoRepository: GitHubRepoRepository,
) : GitHubRepoFindByIdUseCase {
    override fun execute(id: Long): Either<GitHubRepoFindByIdError, GitHubRepo> =
        GitHubRepoId.of(id.toString())
            .mapLeft { GitHubRepoFindByIdError.InvalidId(it) }
            .flatMap { repoId ->
                gitHubRepoRepository.findById(repoId)
                    .mapLeft { GitHubRepoFindByIdError.FetchFailed(it) }
            }
}
```

### Complex use case (`either{}` DSL with multiple steps)

```kotlin
class GitHubRepoListUseCaseImpl(
    private val gitHubRepoRepository: GitHubRepoRepository,
) : GitHubRepoListUseCase {
    override fun execute(pageNumber: Int, pageSize: Int): Either<GitHubRepoListError, Page<GitHubRepo>> =
        either {
            val validPageNumber = PageNumber.of(pageNumber)
                .mapLeft(GitHubRepoListError::InvalidPageNumber)
                .bind()
            val validPageSize = PageSize.of(pageSize)
                .mapLeft(GitHubRepoListError::InvalidPageSize)
                .bind()
            gitHubRepoRepository.list(validPageNumber, validPageSize)
                .mapLeft(GitHubRepoListError::FetchFailed)
                .bind()
        }
}
```

### Application error sealed class

```kotlin
sealed class GitHubRepoListError {
    data class InvalidPageNumber(val cause: PageNumberError) : GitHubRepoListError()
    data class InvalidPageSize(val cause: PageSizeError) : GitHubRepoListError()
    data class FetchFailed(val cause: GitHubError) : GitHubRepoListError()
}
```

---

## Infrastructure Layer

### Repository implementation with `Either.catch`

```kotlin
class GitHubRepoRepositoryImpl(
    private val jpaRepository: GitHubRepoJpaRepository,
) : GitHubRepoRepository {

    override fun findById(id: GitHubRepoId): Either<GitHubError, GitHubRepo> =
        Either
            .catch {
                jpaRepository
                    .findById(id.value)
                    .orElseThrow { NoSuchElementException("Not found: ${id.value}") }
                    .toDomain()
            }.mapLeft { e ->
                when (e) {
                    is NoSuchElementException -> GitHubError.NotFound(id.value)
                    else -> GitHubError.DatabaseError("Unexpected error in findById", e)
                }
            }

    override fun save(repo: GitHubRepo): Either<GitHubError, GitHubRepo> =
        Either
            .catch { jpaRepository.save(repo.toEntity()).toDomain() }
            .mapLeft { e -> GitHubError.DatabaseError("Unexpected error in save", e) }
}
```

---

## Presentation Layer

### Controller with `.fold()`

```kotlin
@GetMapping("/{id}")
fun findById(@PathVariable id: Long): ResponseEntity<*> =
    gitHubRepoFindByIdUseCase.execute(id).fold(
        ifLeft = { error ->
            ResponseEntity.badRequest().body(ErrorResponse(error.message ?: "Unknown error"))
        },
        ifRight = { repo ->
            ResponseEntity.ok(GitHubRepoResponse.fromDomain(repo))
        },
    )

@GetMapping
fun list(
    @RequestParam(defaultValue = "1") pageNumber: Int,
    @RequestParam(defaultValue = "20") pageSize: Int,
): ResponseEntity<*> =
    gitHubRepoListUseCase.execute(pageNumber, pageSize).fold(
        ifLeft = { error ->
            ResponseEntity.badRequest().body(ErrorResponse(error.message ?: "Unknown error"))
        },
        ifRight = { page ->
            ResponseEntity.ok(GitHubRepoListResponse.fromDomain(page))
        },
    )
```
