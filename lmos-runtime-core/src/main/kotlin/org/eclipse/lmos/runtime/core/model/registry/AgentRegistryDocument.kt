/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.model.registry

import kotlinx.serialization.Serializable // Or Jackson if that's standard
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRouting

@Serializable // Or Jackson annotations
data class AgentRegistryDocument(
    val channelRoutings: List<ChannelRouting>,
)
