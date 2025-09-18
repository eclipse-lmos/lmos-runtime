/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.properties

import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.configuration.TenantConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lmos.runtime")
class RuntimeProperties(
    val tenants: List<TenantConfig> = emptyList(),
    agentRegistry: AgentRegistry,
    openAi: OpenAI? = null,
    cache: Cache,
    disambiguation: Disambiguation,
    val router: Router,
) : RuntimeConfiguration(agentRegistry, openAi, cache, disambiguation) {
    private val log = LoggerFactory.getLogger(RuntimeProperties::class.java)

    init {
        log.info(
            "LmosRuntimeProperties initialized with: " +
                "agentRegistry = $agentRegistry, cache = $cache, router = $router",
        )
    }

    companion object {
        const val RUNTIME_CACHE_TYPE = "lmos.runtime.cache.type"
        const val RUNTIME_CACHE_TYPE_REDIS = "redis"
        const val RUNTIME_CACHE_TYPE_MEMORY = "memory"
        const val RUNTIME_REDIS_CLUSTER = "lmos.runtime.redis.cluster"
    }
}

data class Router(
    val type: Type,
)

enum class Type {
    EXPLICIT,
    LLM,
}
