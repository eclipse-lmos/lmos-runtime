/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    val lmosRouterVersion: String by project
    val langChain4jModulesVersion: String by project
    val langChain4jOpenAiVersion: String by project

    api(project(":lmos-runtime-core"))
    implementation("org.springframework.boot:spring-boot-starter")

    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.eclipse.lmos:lmos-classifier-llm-spring-boot-starter:$lmosRouterVersion")
    implementation("org.eclipse.lmos:lmos-classifier-vector-spring-boot-starter:$lmosRouterVersion")
    implementation("org.eclipse.lmos:lmos-classifier-hybrid-spring-boot-starter:$lmosRouterVersion")

    implementation("dev.langchain4j:langchain4j-open-ai:$langChain4jOpenAiVersion")
    implementation("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-anthropic:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-ollama:$langChain4jModulesVersion")
    implementation("com.azure:azure-identity:1.17.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.eclipse.lmos:lmos-classifier-llm-spring-boot-starter:$lmosRouterVersion")
}
