/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("org.jetbrains.kotlin.plugin.spring")
}

dependencies {
    implementation(enforcedPlatform(project(":lmos-runtime-bom")))
    api(project(":lmos-runtime-core"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.eclipse.lmos:lmos-classifier-llm-spring-boot-starter")
    implementation("org.eclipse.lmos:lmos-classifier-vector-spring-boot-starter")
    implementation("org.eclipse.lmos:lmos-classifier-hybrid-spring-boot-starter")
}
