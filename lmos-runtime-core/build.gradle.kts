/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    id("java-test-fixtures")
}

dependencies {
    api(platform(project(":lmos-runtime-bom")))
    implementation(platform(project(":lmos-runtime-bom")))
    testImplementation(platform(project(":lmos-runtime-bom")))
    testImplementation(platform(project(":lmos-runtime-bom")))

    api("org.eclipse.lmos:lmos-router-llm")
    api("org.eclipse.lmos:arc-agent-client")
    api("org.eclipse.lmos:arc-api")
    api("org.eclipse.lmos:lmos-classifier-core")

    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm")
    implementation("com.charleskorn.kaml:kaml")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.slf4j:slf4j-api")
    implementation("dev.langchain4j:langchain4j")

    testFixturesImplementation("com.marcinziolo:kotlin-wiremock")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter")
}
