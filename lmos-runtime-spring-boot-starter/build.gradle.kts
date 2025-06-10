/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */
dependencies {

    val springBootVersion: String by rootProject.extra

    api(project(":lmos-runtime-core"))
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
}
