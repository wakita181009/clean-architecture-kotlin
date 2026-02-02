import org.jooq.meta.jaxb.MatcherRule

plugins {
    kotlin("jvm")
    alias(libs.plugins.spring)
    alias(libs.plugins.spring.dependencies)
    alias(libs.plugins.jooq.codegen)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    kotlin("plugin.spring")
}

ktlint {
    filter {
        exclude("**/postgres_gen/**")
        exclude("**/generated/**")
    }
}

dependencies {
    api(project(":application"))
    // Spring Boot
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-data-r2dbc")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    // Database
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")
    api("jakarta.validation:jakarta.validation-api:3.1.1")
    api(libs.jooq)
    api(libs.jooq.kotlin)
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
    api("io.projectreactor.kotlin:reactor-kotlin-extensions:1.3.0")
    // Fix reactive-streams: It seems org.jooq.Publisher (version 1.0.3) and org.reactivestreams.Publisher (version 1.0.4) are incompatible.
    api("org.reactivestreams:reactive-streams:1.0.4")
    jooqCodegen("org.postgresql:postgresql:42.7.9")
    runtimeOnly("org.postgresql:postgresql") // JDBC for flyway & jOOQ Generator
    runtimeOnly("org.postgresql:r2dbc-postgresql") // R2DBC for app
    runtimeOnly("io.r2dbc:r2dbc-pool:1.0.2.RELEASE") // R2DBC for app
    implementation(libs.flyway.database.postgresql)
    // OkHttp
    implementation(libs.okhttp)
    // Resilience4j
    implementation(libs.resilience4j.kotlin)
    implementation(libs.resilience4j.retry)
    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(kotlin("stdlib"))
}

// Disable spring boot jar task
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = false
}

// for IDE support
sourceSets {
    main {
        kotlin {
            srcDir("src/generated/kotlin")
        }
    }
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://${System.getenv("POSTGRES_HOST")}:${System.getenv("POSTGRES_PORT")}" +
                "/${System.getenv("POSTGRES_DATABASE")}"
            user = System.getenv("POSTGRES_USER")
            password = System.getenv("POSTGRES_PASSWORD")
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                inputSchema = "public"
                excludes = "flyway_schema_history"
            }
            target {
                directory = "src/generated/kotlin"
                packageName = "com.wakita181009.cleanarchitecture.infrastructure.postgres_gen"
            }
            strategy {
                name = "org.jooq.codegen.DefaultGeneratorStrategy"
                matchers {
                    tables {
                        table {
                            tableClass =
                                MatcherRule().apply {
                                    transform = org.jooq.meta.jaxb.MatcherTransformType.PASCAL
                                    expression = "$0_Table"
                                }
                        }
                    }
                    enums {
                        enum_ {
                            enumClass =
                                MatcherRule().apply {
                                    transform = org.jooq.meta.jaxb.MatcherTransformType.PASCAL
                                    expression = "$0_Enum"
                                }
                        }
                    }
                }
            }
        }
    }
}
repositories {
    mavenCentral()
}
