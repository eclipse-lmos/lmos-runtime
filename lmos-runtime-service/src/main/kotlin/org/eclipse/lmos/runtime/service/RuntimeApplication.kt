/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime

import org.eclipse.lmos.runtime.service.properties.RuntimeCorsProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [RuntimeCorsProperties::class])
class RuntimeApplication

fun main(args: Array<String>) {
    runApplication<RuntimeApplication>(*args)
}
