/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

dependencies {

    implementation(project(":lmos-kernel"))
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.2")
    implementation("gg.jte:jte:3.1.16")
    implementation("gg.jte:jte-kotlin:3.1.16")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.0")
    implementation("com.github.pemistahl:lingua:1.2.2")
    implementation("org.mvel:mvel2:2.5.2.Final")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive:3.5.5")
}
