/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

dependencies {
    val lmosRouterVersion: String by project
    val langChain4jCoreVersion: String by project
    val langChain4jModulesVersion: String by project
    val langChain4jOpenAiVersion: String by project
    val springBootVersion: String by rootProject.extra

    api(project(":lmos-runtime-core"))
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")

    implementation("org.eclipse.lmos:lmos-classifier-llm-spring-boot-starter:$lmosRouterVersion")
    implementation("org.eclipse.lmos:lmos-classifier-vector-spring-boot-starter:$lmosRouterVersion")
    implementation("org.eclipse.lmos:lmos-classifier-hybrid-spring-boot-starter:$lmosRouterVersion")

    implementation("dev.langchain4j:langchain4j-open-ai:$langChain4jOpenAiVersion")
    implementation("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-anthropic:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-ollama:$langChain4jModulesVersion")
    implementation("com.azure:azure-identity:1.18.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.eclipse.lmos:lmos-classifier-llm-spring-boot-starter:$lmosRouterVersion")
}
