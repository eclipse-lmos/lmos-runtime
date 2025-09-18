/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.channel

import org.eclipse.lmos.runtime.core.model.custom.Channel

interface ChannelRepository {
    fun getChannel(
        tenantId: String,
        channelId: String,
        subset: String?,
        namespace: String?,
    ): Channel

    fun getChannels(
        tenantId: String,
        subset: String?,
        namespace: String?,
    ): List<Channel>
}
