package com.example.wuphf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class ExampleApplication {
    @Bean fun processEnvironment() = ProcessEnvironment(System.getenv())

    companion object {
        const val DATABASE_PROFILE = "database"
        const val MEMORY_PROFILE = "memory"

        const val HOST_VARIABLE = "DATABASE_HOST"

        fun profile(host: String?) = if (host.isNullOrBlank()) MEMORY_PROFILE else DATABASE_PROFILE
    }
}

fun main(args: Array<String>) {
    runApplication<ExampleApplication>(*args) {
        setAdditionalProfiles(ExampleApplication.profile(System.getenv(ExampleApplication.HOST_VARIABLE)))
    }
}
