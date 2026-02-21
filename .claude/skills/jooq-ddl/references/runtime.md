# jOOQ Runtime Configuration

## JooqConfig Bean

`infrastructure/src/main/kotlin/.../infrastructure/config/JooqConfig.kt`

```kotlin
@Configuration
@EnableTransactionManagement
class JooqConfig(
    private val cfi: ConnectionFactory,
) {
    @Bean
    fun dsl(): DSLContext =
        DSL.using(
            DSL
                .using(cfi)
                .configuration()
                .derive(
                    Settings()
                        .withRenderQuotedNames(RenderQuotedNames.NEVER)
                        .withRenderNameCase(RenderNameCase.LOWER),
                ),
        )

    @Bean
    fun transactionalOperator(): TransactionalOperator = TransactionalOperator.create(R2dbcTransactionManager(cfi))
}
```

Imports needed:
```kotlin
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
```

### Settings Explanation

| Setting | Value | Effect |
|---------|-------|--------|
| `RenderQuotedNames` | `NEVER` | Omits quotes: `github_repo` instead of `"GITHUB_REPO"` |
| `RenderNameCase` | `LOWER` | Forces lowercase identifier rendering |

Apply when jOOQ codegen generates UPPERCASE names (no `defaultNameCase: as_is` in codegen config).
Without these settings, PostgreSQL receives `"GITHUB_REPO"` and returns `relation does not exist`.

---

## Reactive Repository Patterns

Use `Mono.from()` / `Flux.from()` to wrap jOOQ queries.
**Never use `withContext(Dispatchers.IO)`** — R2DBC is non-blocking and returns `Publisher<T>` directly.

### Required imports

```kotlin
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
```

> **Note**: `awaitSingleOrNull` comes from `kotlinx.coroutines.reactor` (not `.reactive`).

### SELECT — single row

Use `Mono.from()` for single-row lookups:

```kotlin
Mono
    .from(
        dsl
            .selectFrom(GITHUB_REPO)
            .where(GITHUB_REPO.ID.eq(id.value)),
    ).awaitSingleOrNull()  // null if not found
```

### SELECT — list + count (parallel with Mono.zip)

Run both queries concurrently using `Mono.zip` — do NOT await them separately:

```kotlin
val itemsMono =
    Flux
        .from(
            dsl
                .selectFrom(GITHUB_REPO)
                .orderBy(GITHUB_REPO.CREATED_AT.desc())
                .limit(pageSize.value)
                .offset(offset),
        ).map { it.toDomain() }
        .collectList()

val countMono =
    Mono
        .from(
            dsl.selectCount().from(GITHUB_REPO),
        ).map { it.value1() }

Mono.zip(countMono, itemsMono).map { Page(it.t1, it.t2) }.awaitSingle()
```

### INSERT / UPSERT with RETURNING

```kotlin
Mono
    .from(
        dsl
            .insertInto(TABLE)
            .set(TABLE.ID, entity.id)
            // ...
            .onConflict(TABLE.ID)
            .doUpdate()
            .set(TABLE.UPDATED_AT, entity.updatedAt)
            .returning(),
    ).awaitSingle()
    .toDomain()
```

### toDomain() placement

Define `toDomain()` as a **private function inside the repository's `companion object`** — not in a separate entity file:

```kotlin
@Repository
class GitHubRepoRepositoryImpl(
    private val dsl: DSLContext,
) : GitHubRepoRepository {
    companion object {
        private fun GithubRepoRecord.toDomain() =
            GitHubRepo(
                id = GitHubRepoId(id!!),
                owner = GitHubOwner(owner!!),
                // ...
            )
    }

    override suspend fun findById(...) { ... }
}
```

### Wrap with Either (full pattern)

```kotlin
override suspend fun findById(id: GitHubRepoId): Either<GitHubError, GitHubRepo> =
    either {
        val record =
            Either
                .catch {
                    Mono
                        .from(
                            dsl
                                .selectFrom(GITHUB_REPO)
                                .where(GITHUB_REPO.ID.eq(id.value)),
                        ).awaitSingleOrNull()
                }.mapLeft { GitHubError.RepositoryError("Failed to find repo: ${it.message}", it) }
                .bind()
        if (record == null) raise(GitHubError.NotFound(id.value))
        record.toDomain()
    }
```

For operations that can't return null (save, list), skip the `either {}` wrapper:

```kotlin
override suspend fun list(...): Either<GitHubError, Page<GitHubRepo>> =
    Either
        .catch {
            // ... reactive chain
        }.mapLeft { GitHubError.RepositoryError("Failed to list repos: ${it.message}", it) }
```