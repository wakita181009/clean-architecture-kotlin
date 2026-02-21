plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.dgs.codegen)
}

dependencies {
    api(project(":application"))
    implementation(platform(libs.spring.boot.dependencies))
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-webflux")
    implementation(platform(libs.dgs.platform))
    implementation(libs.dgs.spring.graphql.starter)
    implementation(libs.graphql.extended.scalars)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.arrow)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask>().configureEach {
    schemaPaths = mutableListOf("$projectDir/src/main/resources/schema")
    generateClient = false
    typeMapping = mutableMapOf("Long" to "kotlin.Long")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    filter {
        exclude { element ->
            element.file.path.contains("/generated/")
        }
    }
}
