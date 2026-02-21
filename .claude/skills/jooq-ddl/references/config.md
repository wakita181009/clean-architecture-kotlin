# jOOQ Gradle Configuration

## `infrastructure/build.gradle.kts`

```kotlin
import org.jooq.meta.jaxb.MatcherRule
import org.jooq.meta.jaxb.MatcherTransformType

plugins {
    // ... other plugins
    alias(libs.plugins.jooq.codegen)
}

dependencies {
    // ... other deps
    api("org.jooq:jooq")
    jooqCodegen(libs.jooq.meta.extensions)   // version managed via libs.versions.toml
}

jooq {
    configuration {
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties {
                    property {
                        key = "scripts"
                        value = "src/main/resources/db/migration/*.sql"
                    }
                }
            }
            strategy {
                name = "org.jooq.codegen.DefaultGeneratorStrategy"
                matchers {
                    tables {
                        table {
                            tableClass =
                                MatcherRule().apply {
                                    transform = MatcherTransformType.PASCAL
                                    expression = "$0_Table"
                                }
                        }
                    }
                    enums {
                        enum_ {
                            enumClass =
                                MatcherRule().apply {
                                    transform = MatcherTransformType.PASCAL
                                    expression = "$0_Enum"
                                }
                        }
                    }
                }
            }
        }
    }
}

sourceSets.main {
    kotlin {
        srcDir("build/generated-sources/jooq/main")
    }
}

tasks.compileKotlin {
    dependsOn(tasks.named("jooqCodegen"))
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = false
}
```

## `gradle/libs.versions.toml`

```toml
[versions]
jooq = "3.19.30"   # Pin to match the jOOQ version used at runtime

[libraries]
jooq-meta-extensions = { group = "org.jooq", name = "jooq-meta-extensions", version.ref = "jooq" }

[plugins]
jooq-codegen = { id = "org.jooq.jooq-codegen-gradle", version.ref = "jooq" }
```

> **Note**: Spring Boot's BOM manages `org.jooq:jooq` at runtime. The `[versions]` entry
> pins the **codegen plugin** version — keep it in sync with the BOM-managed runtime version.

Check the BOM-managed version:
```
./gradlew :infrastructure:dependencyInsight --dependency jooq-core
```