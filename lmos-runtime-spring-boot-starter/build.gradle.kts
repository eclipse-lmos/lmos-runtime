/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("org.jetbrains.kotlin.plugin.spring")
}

dependencies {
    implementation(platform(project(":lmos-runtime-bom")))
    api(project(":lmos-runtime-core"))
    api("org.eclipse.lmos:lmos-classifier-llm-spring-boot-starter")
    api("org.eclipse.lmos:lmos-classifier-vector-spring-boot-starter")
    api("org.eclipse.lmos:lmos-classifier-hybrid-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}
