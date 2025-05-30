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
        val filePath: String? = null, // Path to the YAML file
    )

    data class OpenAI(
        val provider: String,
        val url: String,
        val key: String,
        val model: String,
        val maxTokens: Int,
        val temperature: Double,
        val format: String,
    )

    data class Cache(
        val ttl: Long,
    )
}
