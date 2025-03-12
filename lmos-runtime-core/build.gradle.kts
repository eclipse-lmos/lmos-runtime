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
    val lmosRouterVersion = "0.1.0"

    val ktorVersion = "3.1.1"
    val junitVersion = "5.12.0"

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("org.slf4j:slf4j-api:2.0.17")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.0")
    api("org.eclipse.lmos:lmos-router-llm:$lmosRouterVersion")
    api("org.eclipse.lmos:arc-agent-client:$arcVersion")
    api("org.eclipse.lmos:arc-api:$arcVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")

    testFixturesImplementation("com.marcinziolo:kotlin-wiremock:2.1.1")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}
