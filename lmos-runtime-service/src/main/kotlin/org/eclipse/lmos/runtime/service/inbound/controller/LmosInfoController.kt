/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.inbound.controller

import kotlinx.coroutines.coroutineScope
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.eclipse.lmos.runtime.core.model.registry.toRoutingInformation
import org.eclipse.lmos.runtime.service.constants.ServiceConstants.Endpoints.BASE_PATH
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BASE_PATH)
class LmosInfoController(
    private val channelRoutingRepository: ChannelRoutingRepository,
) {
    @GetMapping("/agents/{tenantId}/{channelId}")
    suspend fun getAvailableAgents(
        @PathVariable tenantId: String,
        @PathVariable channelId: String,
    ) = coroutineScope {
        channelRoutingRepository.getChannelRouting(tenantId, channelId).toRoutingInformation().agentList
    }
}
