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
    val springBootVersion = "4.0.6"
    val ktorVersion = "3.4.3"
    val kotlinxVersion = "1.11.0"
    val lmosRouterVersion = "0.27.0"
    val arcVersion = "0.223.0"
    val langChain4jVersion = "1.13.1"
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
        api("com.charleskorn.kaml:kaml:0.104.0")
        api("com.expediagroup:graphql-kotlin-spring-server:9.2.0")
        api("org.mockito.kotlin:mockito-kotlin:6.3.0")
        api("app.cash.turbine:turbine:1.2.1")
        api("com.marcinziolo:kotlin-wiremock:2.1.1")
        api("org.testcontainers:testcontainers-junit-jupiter")
        api("org.mvel:mvel2:2.5.2.Final")
    }
}
