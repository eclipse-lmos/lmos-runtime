/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.channelrouting

import org.eclipse.lmos.runtime.core.channelrouting.CachedChannelRoutingRepository
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRouting
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.springframework.cache.annotation.Cacheable

open class DefaultCachedChannelRoutingRepository(
    private val channelRoutingRepository: ChannelRoutingRepository,
) : CachedChannelRoutingRepository {
    @Cacheable("channelRoutings", key = "#conversationId + ':' + #tenantId + ':' + #channelId")
    override fun getChannelRouting(
        conversationId: String,
        tenantId: String,
        channelId: String,
        subset: String?,
        namespace: String?,
    ): ChannelRouting = channelRoutingRepository.getChannelRouting(tenantId, channelId, subset, namespace)
}
