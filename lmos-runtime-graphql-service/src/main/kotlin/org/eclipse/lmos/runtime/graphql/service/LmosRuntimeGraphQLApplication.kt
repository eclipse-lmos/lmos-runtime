/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.graphql.service

import org.eclipse.lmos.runtime.graphql.service.properties.LmosRuntimeCorsProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [LmosRuntimeCorsProperties::class])
open class LmosRuntimeGraphQLApplication

fun main(args: Array<String>) {
    runApplication<LmosRuntimeGraphQLApplication>(*args)
}
