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
    val kotlinPluginVersion: String by settings
    val springBootPluginVersion: String by settings
    val ktlintPluginVersion: String by settings
    val dependencyManagementPluginVersion: String by settings
    val licenserPluginVersion: String by settings
    val helmPluginVersion: String by settings
    val releasePluginVersion: String by settings
    val mavenPublishPluginVersion: String by settings
    val koverPluginVersion: String by settings
    val dokkaPluginVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    plugins {
        id("org.springframework.boot") version springBootPluginVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintPluginVersion
        id("io.spring.dependency-management") version dependencyManagementPluginVersion
        id("org.cadixdev.licenser") version licenserPluginVersion
        id("com.citi.helm") version helmPluginVersion
        id("com.citi.helm-publish") version helmPluginVersion
        id("net.researchgate.release") version releasePluginVersion
        id("com.vanniktech.maven.publish") version mavenPublishPluginVersion
        id("org.jetbrains.kotlin.jvm") version kotlinPluginVersion
        id("org.jetbrains.kotlin.kapt") version kotlinPluginVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinPluginVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinPluginVersion
        id("org.jetbrains.kotlinx.kover") version koverPluginVersion
        id("org.jetbrains.dokka") version dokkaPluginVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
