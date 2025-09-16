/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.channelrouting

import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.eclipse.lmos.runtime.core.model.registry.ChannelRouting
import org.springframework.cache.annotation.Cacheable

open class CachedChannelRoutingRepository(
    private val channelRoutingRepository: ChannelRoutingRepository,
) : ChannelRoutingRepository {
    @Cacheable("channelRoutings", key = "#conversationId + ':' + #tenantId + ':' + #channelId")
    override fun getChannelRouting(
        conversationId: String,
        tenantId: String,
        channelId: String,
        subset: String?,
    ): ChannelRouting = channelRoutingRepository.getChannelRouting(conversationId, tenantId, channelId, subset)
}
