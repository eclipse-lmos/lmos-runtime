/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core

import org.eclipse.lmos.runtime.core.disambiguation.defaultDisambiguationClarificationPrompt
import org.eclipse.lmos.runtime.core.disambiguation.defaultDisambiguationIntroductionPrompt

open class RuntimeConfiguration(
    val agentRegistry: AgentRegistry,
    val openAi: OpenAI? = null,
    val cache: Cache,
    val disambiguation: Disambiguation,
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

    data class Disambiguation(
        val enabled: Boolean = false,
        val introductionPrompt: String? = null,
        val clarificationPrompt: String? = null,
        val llm: ChatModel,
    ) {
        fun introductionPrompt() = if (introductionPrompt.isNullOrEmpty()) defaultDisambiguationIntroductionPrompt() else introductionPrompt

        fun clarificationPrompt() =
            if (clarificationPrompt.isNullOrEmpty()) defaultDisambiguationClarificationPrompt() else clarificationPrompt
    }

    data class ChatModel(
        val provider: String,
        val apiKey: String? = null,
        val baseUrl: String? = null,
        val model: String,
        val maxTokens: Int = 2000,
        val temperature: Double = 0.0,
        val logRequestsAndResponses: Boolean = false,
        val systemPrompt: String = "",
    )

    data class Cache(
        val ttl: Long,
    )
}
