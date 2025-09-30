/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.channelrouting

interface ChannelRoutingRepository {
    fun getChannelRouting(
        tenantId: String,
        channelId: String,
        subset: String? = null,
        namespace: String? = null,
    ): ChannelRouting
}
