/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("java-platform")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    val springBootVersion = "3.5.6"
    val ktorVersion = "3.4.0"
    val kotlinxVersion = "1.9.0"
    val lmosRouterVersion = "0.22.0"
    val arcVersion = "0.207.0"
    val langChain4jVersion = "1.5.0"
    val kotlinCoroutines = "1.10.2"

    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    api(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:$kotlinxVersion"))
    api(platform("io.ktor:ktor-bom:$ktorVersion"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$kotlinCoroutines"))
    api(platform("dev.langchain4j:langchain4j-bom:$langChain4jVersion"))

    constraints {
        // lmosRouterVersion-managed
        api("org.eclipse.lmos:lmos-classifier-llm-spring-boot-starter:$lmosRouterVersion")
        api("org.eclipse.lmos:lmos-classifier-vector-spring-boot-starter:$lmosRouterVersion")
        api("org.eclipse.lmos:lmos-classifier-hybrid-spring-boot-starter:$lmosRouterVersion")
        api("org.eclipse.lmos:lmos-classifier-core:$lmosRouterVersion")
        api("org.eclipse.lmos:lmos-classifier-llm:$lmosRouterVersion")
        api("org.eclipse.lmos:lmos-router-llm:$lmosRouterVersion")
        // arcVersion-managed
        api("org.eclipse.lmos:arc-agent-client:$arcVersion")
        api("org.eclipse.lmos:arc-api:$arcVersion")

        // Misc
        api("com.charleskorn.kaml:kaml:0.93.0")
        api("com.expediagroup:graphql-kotlin-spring-server:8.8.1")
        api("org.mockito.kotlin:mockito-kotlin:6.2.2")
        api("app.cash.turbine:turbine:1.2.1")
        api("com.marcinziolo:kotlin-wiremock:2.1.1")
        api("org.testcontainers:junit-jupiter:1.21.4")
        api("com.redis.testcontainers:testcontainers-redis:1.6.4")
    }
}
