/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.inbound.controller

import kotlinx.coroutines.coroutineScope
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.service.constants.LmosServiceConstants.Endpoints.BASE_PATH
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$BASE_PATH/agents")
class AgentsController(
    private val agentRegistryService: AgentRegistryService,
) {
    data class CapabilitySummary(
        val id: String,
        val name: String,
        val version: String,
        val description: String,
    )

    data class AgentSummary(
        val id: String,
        val name: String,
        val version: String,
        val description: String,
        val capabilities: List<CapabilitySummary>,
    )

    data class AgentsResponse(
        val subset: String?,
        val agents: List<AgentSummary>,
    )

    /**
     * Returns a list of agents (flattened from the routing information) for the given tenant, channel and optional subset.
     * Example: GET /lmos/runtime/apis/v1/agents?tenantId=tenantA&channelId=web&subset=beta
     */
    @GetMapping
    suspend fun getAgents(
        @RequestParam tenantId: String,
        @RequestParam channelId: String,
        @RequestParam(required = false) subset: String?,
    ): AgentsResponse =
        coroutineScope {
            val routingInformation = agentRegistryService.getRoutingInformation(tenantId, channelId, subset)
            AgentsResponse(
                subset = routingInformation.subset,
                agents =
                    routingInformation.agentList.map { agent ->
                        AgentSummary(
                            id = agent.id,
                            name = agent.name,
                            version = agent.version,
                            description = agent.description,
                            capabilities =
                                agent.capabilities.map { c ->
                                    CapabilitySummary(
                                        id = c.id,
                                        name = c.name,
                                        version = c.version,
                                        description = c.description,
                                    )
                                },
                        )
                    },
            )
        }
}
