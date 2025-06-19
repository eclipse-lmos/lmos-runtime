/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.service.outbound

import org.eclipse.lmos.runtime.core.model.registry.RoutingInformation

interface AgentRegistryService {
    suspend fun getRoutingInformation(
        tenantId: String,
        channelId: String,
        subset: String? = null,
    ): RoutingInformation
}
