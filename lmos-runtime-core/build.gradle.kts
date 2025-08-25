/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    `java-test-fixtures`
}

dependencies {

    val arcVersion = "0.154.0"
    val lmosRouterVersion: String by project

    val ktorVersion = "3.2.3"
    val junitVersion = "5.13.4"
    val kotlinxSerializationVersion = "1.9.0"

    val langChain4jCoreVersion: String by project
    val jacksonVersion: String by project

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
    implementation("com.charleskorn.kaml:kaml:0.92.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.2")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.eclipse.lmos:lmos-classifier-core:$lmosRouterVersion")
    implementation("dev.langchain4j:langchain4j:$langChain4jCoreVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
    api("org.eclipse.lmos:lmos-router-llm:$lmosRouterVersion")
    api("org.eclipse.lmos:arc-agent-client:$arcVersion")
    api("org.eclipse.lmos:arc-api:$arcVersion")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    testFixturesImplementation("com.marcinziolo:kotlin-wiremock:2.1.1")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}
