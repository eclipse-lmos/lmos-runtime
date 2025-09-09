/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

rootProject.name = "lmos-runtime"

include("lmos-kernel")
include("lmos-kernel-impl")
include("lmos-runtime-core")
include("lmos-runtime-spring-boot-starter")
include("lmos-runtime-service")
include("lmos-runtime-graphql-service")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
