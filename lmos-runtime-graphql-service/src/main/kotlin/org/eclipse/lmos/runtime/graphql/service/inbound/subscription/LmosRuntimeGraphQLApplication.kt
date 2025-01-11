/*
 * SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.lmos.runtime.graphql.service.inbound.subscription

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class LmosRuntimeGraphQLApplication

fun main(args: Array<String>) {
    runApplication<LmosRuntimeGraphQLApplication>(*args)
}
