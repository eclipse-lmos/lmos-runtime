/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.properties

import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lmos.runtime")
class RuntimeProperties(
    channelRoutingRepository: ChannelRoutingRepositoryConfig,
    openAi: OpenAI? = null,
    disambiguation: Disambiguation,
    val router: Router,
) : RuntimeConfiguration(channelRoutingRepository, openAi, disambiguation)

data class Router(
    val type: Type,
)

enum class Type {
    EXPLICIT,
    LLM,
}
