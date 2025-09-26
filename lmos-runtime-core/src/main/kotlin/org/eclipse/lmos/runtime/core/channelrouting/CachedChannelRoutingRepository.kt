/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.channelrouting

interface CachedChannelRoutingRepository {
    fun getChannelRouting(
        conversationId: String,
        tenantId: String,
        channelId: String,
        subset: String?,
        namespace: String?,
    ): ChannelRouting
}
