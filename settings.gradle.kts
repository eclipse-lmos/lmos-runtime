/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

rootProject.name = "lmos-runtime"

include("lmos-runtime-core")
include("lmos-runtime-spring-boot-starter")
include("lmos-runtime-service")
include("lmos-runtime-graphql-service")
include("lmos-runtime-bom")

pluginManagement {
    val kotlinVersion = "2.2.20"
    val helmVersion = "2.2.0"

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    plugins {
        id("org.springframework.boot") version "4.0.1"
        id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
        id("io.spring.dependency-management") version "1.1.7"
        id("org.cadixdev.licenser") version "0.6.1"
        id("com.citi.helm") version helmVersion
        id("com.citi.helm-publish") version helmVersion
        id("net.researchgate.release") version "3.1.0"
        id("com.vanniktech.maven.publish") version "0.34.0"
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.kapt") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.jetbrains.kotlinx.kover") version "0.9.4"
        id("org.jetbrains.dokka") version "2.0.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
