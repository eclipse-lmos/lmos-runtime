/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    `java-test-fixtures`
}

dependencies {

    val arcVersion = "0.121.0"
    val lmosRouterVersion = "0.3.0"

    val ktorVersion = "3.2.0"
    val junitVersion = "5.13.3"
    val kotlinxSerializationVersion = "1.8.1"

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
    implementation("com.charleskorn.kaml:kaml:0.55.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
    implementation("org.slf4j:slf4j-api:2.0.17")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
    api("org.eclipse.lmos:lmos-router-llm:$lmosRouterVersion")
    api("org.eclipse.lmos:arc-agent-client:$arcVersion")
    api("org.eclipse.lmos:arc-api:$arcVersion")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    testFixturesImplementation("com.marcinziolo:kotlin-wiremock:2.1.1")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}
