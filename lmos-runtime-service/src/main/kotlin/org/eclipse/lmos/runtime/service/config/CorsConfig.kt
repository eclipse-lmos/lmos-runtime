/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.config

import org.eclipse.lmos.runtime.service.properties.RuntimeCorsProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
open class CorsConfig(
    private val runtimeCorsProperties: RuntimeCorsProperties,
) {
    private val log = LoggerFactory.getLogger(CorsConfig::class.java)

    @Bean
    @ConditionalOnProperty(prefix = "lmos.runtime", name = ["cors.enabled"], havingValue = "true", matchIfMissing = false)
    open fun corsWebFilter(): CorsWebFilter {
        val corsConfig =
            CorsConfiguration().apply {
                allowedOrigins = runtimeCorsProperties.allowedOrigins
                maxAge = runtimeCorsProperties.maxAge
                allowedMethods = runtimeCorsProperties.allowedMethods
                allowedHeaders = runtimeCorsProperties.allowedHeaders
            }

        log.info(
            "CORS Configuration: Allowed Origins: {}, MaxAge: {}, Allowed Methods: {}, Allowed Headers: {}",
            corsConfig.allowedOrigins?.joinToString(", "),
            corsConfig.maxAge,
            corsConfig.allowedMethods?.joinToString(", "),
            corsConfig.allowedHeaders?.joinToString(", "),
        )

        val source =
            UrlBasedCorsConfigurationSource().apply {
                runtimeCorsProperties.patterns.forEach { pattern ->
                    registerCorsConfiguration(pattern, corsConfig)
                }
            }
        return CorsWebFilter(source)
    }
}
