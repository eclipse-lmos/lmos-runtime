/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.graphql.service.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lmos.runtime.cors")
class LmosRuntimeCorsProperties(
    var enabled: Boolean = false,
    var allowedOrigins: List<String> = emptyList(),
    var allowedMethods: List<String> = emptyList(),
    var allowedHeaders: List<String> = emptyList(),
    var patterns: List<String> = emptyList(),
    var maxAge: Long = 8000,
)
