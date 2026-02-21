import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

allprojects {
    group = "com.wakita181009.cleanarchitecture"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "25"
            targetCompatibility = "25"
        }
        withType<KotlinCompile> {
            compilerOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
                jvmTarget = JvmTarget.JVM_25
            }
        }
        withType<Test> {
            reports.junitXml.required.set(true)
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")
}

kover {
    merge {
        projects(":domain", ":application", ":presentation")
    }
    reports {
        verify {
            rule {
                bound {
                    minValue = 80
                }
            }
        }
    }
}

val detektClasspath: Configuration by configurations.creating
val detektTask =
    tasks.register<JavaExec>("detekt") {
        mainClass.set("dev.detekt.cli.Main")
        classpath = detektClasspath
        dependsOn(":detekt-rules:jar")

        val input = projectDir
        val config = "$projectDir/detekt.yml"
        val exclude = ".*/build/.*,.*/resources/.*"

        args("--input", input, "--config", config, "--excludes", exclude)
        // argumentProviders is evaluated at execution time to resolve the plugin jar path
        argumentProviders.add {
            listOf(
                "--plugins",
                project(":detekt-rules")
                    .tasks
                    .getByName("jar")
                    .outputs.files.asPath,
            )
        }
    }

tasks.check {
    dependsOn(detektTask)
}

dependencies {
    implementation(kotlin("stdlib"))
    detektClasspath(libs.detekt.cli)
}

repositories {
    mavenCentral()
}
