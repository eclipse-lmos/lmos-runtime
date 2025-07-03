/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core

open class LmosRuntimeConfig(
    val agentRegistry: AgentRegistry,
    val openAi: OpenAI? = null,
    val cache: Cache,
) {
    data class AgentRegistry(
        val baseUrl: String? = null, // Made nullable
        val type: AgentRegistryType = AgentRegistryType.API, // Default to API
        val fileName: String? = null, // Path to the YAML file
        val defaultSubset: String = "stable",
    )

    data class OpenAI(
        val provider: String? = null,
        val url: String? = null,
        val key: String? = null,
        val model: String? = null,
        val maxTokens: Int? = null,
        val temperature: Double? = null,
        val format: String? = null,
    )

    data class Cache(
        val ttl: Long,
    )
}
