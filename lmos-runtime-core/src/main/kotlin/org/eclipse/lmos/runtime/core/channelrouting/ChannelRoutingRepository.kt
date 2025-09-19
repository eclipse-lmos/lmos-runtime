/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.channelrouting

import org.eclipse.lmos.runtime.core.model.registry.ChannelRouting

interface ChannelRoutingRepository {
    fun getChannelRouting(
        tenantId: String,
        channelId: String,
        subset: String? = null,
        namespace: String? = null,
    ): ChannelRouting
}
