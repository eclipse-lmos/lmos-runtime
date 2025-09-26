/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.inbound.controller

import org.eclipse.lmos.runtime.core.channel.Channel
import org.eclipse.lmos.runtime.core.channel.ChannelRepository
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRouting
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.eclipse.lmos.runtime.core.constants.RuntimeConstants.NAMESPACE
import org.eclipse.lmos.runtime.core.exception.NoChannelFoundException
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.service.constants.ServiceConstants.Endpoints.BASE_PATH
import org.eclipse.lmos.runtime.service.constants.ServiceConstants.Headers.SUBSET
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("$BASE_PATH/tenants/{tenantId}/channels")
class CustomResourcesController(
    private val channelRoutingRepository: ChannelRoutingRepository,
    private val channelRepository: ChannelRepository,
) {
    private val log = LoggerFactory.getLogger(CustomResourcesController::class.java)

    @GetMapping
    suspend fun listChannels(
        @PathVariable tenantId: String,
        @RequestHeader(name = SUBSET, required = false) subset: String?,
        @RequestHeader(name = NAMESPACE, required = false) namespace: String?,
    ): List<Channel> {
        log.debug("List channels tenant=$tenantId subset=$subset namespace=$namespace")
        return channelRepository.getChannels(tenantId, subset, namespace)
    }

    @GetMapping("/{channelId}")
    suspend fun getChannel(
        @PathVariable tenantId: String,
        @PathVariable channelId: String,
        @RequestHeader(name = SUBSET, required = false) subset: String?,
        @RequestHeader(name = NAMESPACE, required = false) namespace: String?,
    ): Channel {
        log.debug("Get channel tenant=$tenantId channel=$channelId subset=$subset namespace=$namespace")
        return channelRepository.getChannel(tenantId, channelId, subset, namespace)
    }

    @GetMapping("/{channelId}/routing")
    suspend fun getChannelRouting(
        @PathVariable tenantId: String,
        @PathVariable channelId: String,
        @RequestHeader(name = SUBSET, required = false) subset: String?,
        @RequestHeader(name = NAMESPACE, required = false) namespace: String?,
    ): ChannelRouting {
        log.debug("Get channel routing tenant=$tenantId channel=$channelId subset=$subset namespace=$namespace")

        return channelRoutingRepository.getChannelRouting(
            tenantId = tenantId,
            channelId = channelId,
            subset = subset,
            namespace = namespace,
        )
    }

    @ExceptionHandler(NoRoutingInfoFoundException::class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    fun handleNoRoutingInfoFoundException(ex: NoRoutingInfoFoundException): Map<String, String> =
        mapOf("error" to (ex.message ?: "No routing information found"))

    @ExceptionHandler(NoChannelFoundException::class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    fun handleNoChannelFoundException(ex: NoChannelFoundException): Map<String, String> =
        mapOf("error" to (ex.message ?: "No channel found"))
}
