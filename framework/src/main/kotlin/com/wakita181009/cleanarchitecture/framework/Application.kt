package com.wakita181009.cleanarchitecture.framework

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(
    basePackages = [
        "com.wakita181009.cleanarchitecture.infrastructure",
        "com.wakita181009.cleanarchitecture.presentation",
        "com.wakita181009.cleanarchitecture.framework",
    ],
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
