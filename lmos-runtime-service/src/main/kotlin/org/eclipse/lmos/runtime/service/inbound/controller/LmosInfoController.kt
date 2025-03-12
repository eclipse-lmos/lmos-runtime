/*
 * // SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.inbound.controller

import kotlinx.coroutines.coroutineScope
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.service.constants.LmosServiceConstants.Endpoints.BASE_PATH
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BASE_PATH)
class LmosInfoController(
    private val agentRegistryService: AgentRegistryService,
) {
    @GetMapping("/agents/{tenantId}/{channelId}")
    suspend fun getAvailableAgents(
        @PathVariable tenantId: String,
        @PathVariable channelId: String,
    ) = coroutineScope {
        agentRegistryService.getRoutingInformation(tenantId, channelId).agentList
    }
}
