/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.graphql.service.config

import org.eclipse.lmos.runtime.graphql.service.properties.LmosRuntimeCorsProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
open class CorsConfig(private val runtimeCorsProperties: LmosRuntimeCorsProperties) : WebFluxConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        if (runtimeCorsProperties.enabled) {
            runtimeCorsProperties.patterns.forEach { pattern ->
                registry
                    .addMapping(pattern)
                    .allowedOrigins(*runtimeCorsProperties.allowedOrigins.toTypedArray())
                    .allowedMethods(*runtimeCorsProperties.allowedMethods.toTypedArray())
                    .allowedHeaders(*runtimeCorsProperties.allowedHeaders.toTypedArray())
                    .maxAge(runtimeCorsProperties.maxAge)
            }
        }
    }
}
