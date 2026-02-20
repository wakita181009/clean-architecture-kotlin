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
}

dependencies {
    implementation(kotlin("stdlib"))
}

repositories {
    mavenCentral()
}
