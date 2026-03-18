import org.jooq.meta.jaxb.MatcherRule
import org.jooq.meta.jaxb.MatcherTransformType

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.jooq.codegen)
}

dependencies {
    api(project(":application"))
    implementation(platform(libs.spring.boot.dependencies))
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    implementation(libs.flyway.database.postgresql)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(kotlin("stdlib"))
    api("org.jooq:jooq")

    jooqCodegen(libs.jooq.meta.extensions)
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
