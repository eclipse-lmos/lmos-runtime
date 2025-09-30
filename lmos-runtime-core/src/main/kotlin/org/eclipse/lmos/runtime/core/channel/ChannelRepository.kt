/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.channel

interface ChannelRepository {
    fun getChannel(
        tenantId: String,
        channelId: String,
        subset: String? = null,
        namespace: String? = null,
    ): Channel

    fun getChannels(
        tenantId: String,
        subset: String? = null,
        namespace: String? = null,
    ): List<Channel>
}
