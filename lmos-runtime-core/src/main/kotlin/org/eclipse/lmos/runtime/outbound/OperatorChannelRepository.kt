/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.outbound

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.channel.ChannelRepository
import org.eclipse.lmos.runtime.core.constants.RuntimeConstants.NAMESPACE
import org.eclipse.lmos.runtime.core.constants.RuntimeConstants.SUBSET
import org.eclipse.lmos.runtime.core.exception.InternalServerErrorException
import org.eclipse.lmos.runtime.core.exception.NoChannelFoundException
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.exception.UnexpectedResponseException
import org.eclipse.lmos.runtime.core.model.custom.Channel
import org.slf4j.LoggerFactory

/**
 * Repository implementation for interacting with operator channels.
 * Provides methods to retrieve single or multiple channel resources
 * from the operator's API.
 *
 * @property lmosRuntimeConfig Configuration object for runtime settings.
 */
class OperatorChannelRepository(
    private val lmosRuntimeConfig: RuntimeConfiguration,
) : ChannelRepository {
    /**
     * HTTP client with CIO engine and JSON content negotiation.
     */
    private val client: HttpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
        }

    /**
     * Logger instance for repository operations.
     */
    private val log = LoggerFactory.getLogger(OperatorChannelRepository::class.java)

    /**
     * Retrieves a single channel resource for a given tenant and channel ID.
     *
     * @param tenantId The ID of the tenant.
     * @param channelId The ID of the channel.
     * @param subset Optional value for the subset header.
     * @param namespace Optional value for the namespace header.
     * @return The retrieved [Channel].
     * @throws InternalServerErrorException If an error occurs during the HTTP request.
     * @throws NoRoutingInfoFoundException If the channel is not found (404).
     * @throws UnexpectedResponseException If the response is invalid or unexpected.
     */
    override fun getChannel(
        tenantId: String,
        channelId: String,
        subset: String?,
        namespace: String?,
    ): Channel =
        runBlocking {
            val urlString = "${lmosRuntimeConfig.agentRegistry.baseUrl}/apis/v1/tenants/$tenantId/channels/$channelId"
            log.trace("Calling operator(getChannel): $urlString subset=$subset namespace=$namespace")
            val response =
                try {
                    client.get(urlString) {
                        subset?.let { headers.append(SUBSET, it) }
                        namespace?.let { headers.append(NAMESPACE, it) }
                    }
                } catch (e: Exception) {
                    log.error("Exception getChannel: $urlString", e)
                    throw InternalServerErrorException("Operator error getChannel")
                }
            when {
                response.status.value == 404 -> {
                    log.warn("Channel not found tenant=$tenantId channel=$channelId")
                    throw NoChannelFoundException("Channel not found for tenant=$tenantId channel=$channelId")
                }
                !response.status.isSuccess() -> {
                    val body = response.bodyAsText()
                    log.error("Operator getChannel failed: ${response.status.value} body=$body")
                    throw UnexpectedResponseException("Unexpected response getting channel")
                }
            }
            try {
                response.body<Channel>()
            } catch (e: Exception) {
                log.error("Deserialization error getChannel body=${response.bodyAsText()}", e)
                throw UnexpectedResponseException("Invalid channel body: ${e.message}")
            }
        }

    /**
     * Retrieves a list of channel resources for a given tenant.
     *
     * @param tenantId The ID of the tenant.
     * @param subset Optional value for the subset header.
     * @param namespace Optional value for the namespace header.
     * @return A list of [Channel] objects.
     * @throws InternalServerErrorException If an error occurs during the HTTP request.
     * @throws UnexpectedResponseException If the response is invalid or unexpected.
     */
    override fun getChannels(
        tenantId: String,
        subset: String?,
        namespace: String?,
    ): List<Channel> =
        runBlocking {
            val urlString = "${lmosRuntimeConfig.agentRegistry.baseUrl}/apis/v1/tenants/$tenantId/channels"
            log.trace("Calling operator(listChannels): $urlString subset=$subset namespace=$namespace")
            val response =
                try {
                    client.get(urlString) {
                        subset?.let { headers.append(SUBSET, it) }
                        namespace?.let { headers.append(NAMESPACE, it) }
                    }
                } catch (e: Exception) {
                    log.error("Exception listChannels: $urlString", e)
                    throw InternalServerErrorException("Operator error listChannels")
                }
            if (!response.status.isSuccess()) {
                val body = response.bodyAsText()
                log.error("Operator listChannels failed: ${response.status.value} body=$body")
                throw UnexpectedResponseException("Unexpected response listing channels")
            }
            try {
                response.body<List<Channel>>()
            } catch (e: Exception) {
                log.error("Deserialization error listChannels body=${response.bodyAsText()}", e)
                throw UnexpectedResponseException("Invalid channel list body: ${e.message}")
            }
        }
}
