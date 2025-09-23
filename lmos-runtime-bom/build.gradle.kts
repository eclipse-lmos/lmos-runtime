/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("java-platform")
    id("maven-publish")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    val springBootVersion = "3.5.5"
    val ktorVersion = "3.2.3"
    val kotlinxSerializationVersion = "1.9.0"
    val junitVersion = "5.13.4"
    val jacksonVersion = "2.19.0"
    val lmosRouterVersion = "0.9.0"
    val arcVersion = "0.154.0"
    val langChain4jCoreVersion = "1.0.0"
    val langChain4jModulesVersion = "1.0.1-beta6"
    val langChain4jOpenAiVersion = "1.0.0"

    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    constraints {
        // Ktor
        api("io.ktor:ktor-client-cio:$ktorVersion")
        api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
        api("io.ktor:ktor-serialization-jackson:$ktorVersion")
        api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
        // kotlinx
        api("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
        api("org.jetbrains.kotlinx:kotlinx-serialization-bom:$kotlinxSerializationVersion")
        api("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
        api("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        // JUnit
        api("org.junit.jupiter:junit-jupiter:$junitVersion")
        // Jackson
        api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        // Spring Boot
        api("org.springframework.boot:spring-boot-starter:$springBootVersion")
        api("org.springframework.boot:spring-boot-starter-cache:$springBootVersion")
        api("org.springframework.boot:spring-boot-starter-data-redis:$springBootVersion")
        api("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")
        api("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
        api("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
        api("org.springframework.boot:spring-boot-starter-data-redis-reactive:$springBootVersion")
        // lmosRouterVersion-managed
        api("org.eclipse.lmos:lmos-classifier-llm-spring-boot-starter:$lmosRouterVersion")
        api("org.eclipse.lmos:lmos-classifier-vector-spring-boot-starter:$lmosRouterVersion")
        api("org.eclipse.lmos:lmos-classifier-hybrid-spring-boot-starter:$lmosRouterVersion")
        api("org.eclipse.lmos:lmos-classifier-core:$lmosRouterVersion")
        api("org.eclipse.lmos:lmos-router-llm:$lmosRouterVersion")
        // arcVersion-managed
        api("org.eclipse.lmos:arc-agent-client:$arcVersion")
        api("org.eclipse.lmos:arc-api:$arcVersion")
        // langChain4j
        api("dev.langchain4j:langchain4j:$langChain4jCoreVersion")
        api("dev.langchain4j:langchain4j-open-ai:$langChain4jOpenAiVersion")
        api("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jModulesVersion")
        api("dev.langchain4j:langchain4j-anthropic:$langChain4jModulesVersion")
        api("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jModulesVersion")
        api("dev.langchain4j:langchain4j-ollama:$langChain4jModulesVersion")
        // Misc
        api("com.azure:azure-identity:1.17.0")
        api("com.charleskorn.kaml:kaml:0.93.0")
        api("org.slf4j:slf4j-api:2.0.17")
        api("com.expediagroup:graphql-kotlin-spring-server:8.8.1")
        api("com.github.ben-manes.caffeine:caffeine:3.2.2")
        api("gg.jte:jte:3.1.16")
        api("gg.jte:jte-kotlin:3.1.16")
        api("com.github.pemistahl:lingua:1.2.2")
        api("org.mvel:mvel2:2.5.2.Final")
        api("org.assertj:assertj-core:3.27.4")
        api("org.mockito.kotlin:mockito-kotlin:6.0.0")
        api("app.cash.turbine:turbine:1.2.1")
        api("com.marcinziolo:kotlin-wiremock:2.1.1")
        api("org.testcontainers:junit-jupiter:1.15.3")
        api("com.redis.testcontainers:testcontainers-redis:1.6.4")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenBom") {
            from(components["javaPlatform"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            pom {
                name.set("LMOS Runtime BOM")
                description.set("A Bill of Materials (BOM) for LMOS Runtime dependencies.")
                url.set("https://github.com/eclipse-lmos/lmos-runtime")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://github.com/eclipse-lmos/lmos-runtime/blob/main/LICENSES/Apache-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/eclipse-lmos/lmos-runtime.git")
                }
            }
        }
    }
}
