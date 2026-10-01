plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.spring") version "2.4.20"
    application
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["kotlin.version"] = "2.4.20"

extra["testcontainers.version"] = "2.0.5"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-restclient")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("io.kotest:kotest-assertions-core:6.2.5")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

application {
    mainClass = "com.example.wuphf.ExampleApplicationKt"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<Zip>("distZip") {
    archiveFileName = "java-spring-wuphf.zip"
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName = "java-spring-wuphf.jar"
}

// IMAGE picks the repository the image goes to, ghcr.io/pellepelster/java-spring-wuphf on CI
val image = providers.environmentVariable("IMAGE").getOrElse("java-spring-wuphf")

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName = "$image:latest"
    tags = listOf("$image:${project.version}")

    // only used with --publishImage
    docker {
        publishRegistry {
            username = providers.environmentVariable("REGISTRY_USERNAME").getOrElse("")
            password = providers.environmentVariable("REGISTRY_PASSWORD").getOrElse("")
        }
    }
}

tasks.register("dist") {
    group = "distribution"
    description = "Builds the executable jar at build/libs/java-spring-wuphf.jar and the distribution archive at build/distributions/java-spring-wuphf.zip."
    dependsOn(tasks.named("bootJar"), tasks.named("distZip"))
}
