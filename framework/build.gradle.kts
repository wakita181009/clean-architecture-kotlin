plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    application
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(project(":infrastructure"))
    implementation(project(":presentation"))
    implementation(platform(libs.spring.boot.dependencies))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.kotlinx.coroutines.reactor)
    compileOnly("org.springframework.boot:spring-boot-devtools")
}

application {
    mainClass.set("com.wakita181009.cleanarchitecture.framework.ApplicationKt")
}
