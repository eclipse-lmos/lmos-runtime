/*
 * // SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.graphql.service.inbound.subscription

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import kotlinx.coroutines.coroutineScope
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.springframework.stereotype.Component

@Component
class LmosInfoQuery(
    private val agentRegistryService: AgentRegistryService,
) : Query {
    @GraphQLDescription("Returns the available agents for tenant and channel")
    suspend fun getAvailableAgents(
        tenantId: String,
        channelId: String,
    ) = coroutineScope {
        agentRegistryService.getRoutingInformation(tenantId, channelId).agentList
    }
}
