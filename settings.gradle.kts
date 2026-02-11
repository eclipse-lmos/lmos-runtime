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
    val kotlinVersion = "2.3.10"
    val helmVersion = "3.1.1"

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    plugins {
        id("org.springframework.boot") version "4.0.2"
        id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
        id("io.spring.dependency-management") version "1.1.7"
        id("dev.yumi.gradle.licenser") version "2.2.2"
        id("io.github.build-extensions-oss.helm") version helmVersion
        id("io.github.build-extensions-oss.helm-publish") version helmVersion
        id("net.researchgate.release") version "3.1.0"
        id("com.vanniktech.maven.publish") version "0.36.0"
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.kapt") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.jetbrains.kotlinx.kover") version "0.9.7"
        id("org.jetbrains.dokka") version "2.1.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
