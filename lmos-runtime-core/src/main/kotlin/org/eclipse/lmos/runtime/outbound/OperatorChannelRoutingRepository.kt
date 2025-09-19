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
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.eclipse.lmos.runtime.core.constants.RuntimeConstants.NAMESPACE
import org.eclipse.lmos.runtime.core.constants.RuntimeConstants.SUBSET
import org.eclipse.lmos.runtime.core.exception.InternalServerErrorException
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.exception.UnexpectedResponseException
import org.eclipse.lmos.runtime.core.model.registry.ChannelRouting
import org.slf4j.LoggerFactory

/**
 * Repository implementation for interacting with operator channel routing.
 * Provides methods to retrieve channel routing information from the operator's API.
 *
 * @property runtimeConfig Configuration object for runtime settings.
 */
open class OperatorChannelRoutingRepository(
    private val runtimeConfig: RuntimeConfiguration,
) : ChannelRoutingRepository {
    /**
     * Logger instance for repository operations.
     */
    private val log = LoggerFactory.getLogger(OperatorChannelRoutingRepository::class.java)

    /**
     * HTTP client with CIO engine and JSON content negotiation.
     */
    private val client: HttpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    json =
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                            encodeDefaults = true
                        },
                )
            }
        }

    /**
     * Blocking wrapper around the suspend version of getChannelRouting.
     * Useful for Spring Cache, which doesn't support suspend functions.
     *
     * @param tenantId The ID of the tenant.
     * @param channelId The ID of the channel.
     * @param subset Optional value for the subset header.
     * @param namespace Optional value for the namespace header.
     * @return The retrieved [ChannelRouting].
     * @throws InternalServerErrorException If an error occurs during the HTTP request.
     * @throws NoRoutingInfoFoundException If the routing info is not found (404).
     * @throws UnexpectedResponseException If the response is invalid or unexpected.
     */
    override fun getChannelRouting(
        tenantId: String,
        channelId: String,
        subset: String?,
        namespace: String?,
    ): ChannelRouting =
        runBlocking {
            getChannelRoutingSuspend(tenantId, channelId, subset, namespace)
        }

    /**
     * Retrieves channel routing information for a given tenant and channel ID (suspend version).
     *
     * @param tenantId The ID of the tenant.
     * @param channelId The ID of the channel.
     * @param subset Optional value for the subset header.
     * @param namespace Optional value for the namespace header.
     * @return The retrieved [ChannelRouting].
     * @throws InternalServerErrorException If an error occurs during the HTTP request.
     * @throws NoRoutingInfoFoundException If the routing info is not found (404).
     * @throws UnexpectedResponseException If the response is invalid or unexpected.
     */
    suspend fun getChannelRoutingSuspend(
        tenantId: String,
        channelId: String,
        subset: String?,
        namespace: String?,
    ): ChannelRouting {
        val urlString =
            "${runtimeConfig.agentRegistry.baseUrl}/apis/v1/tenants/$tenantId/channels/$channelId/routing"
        log.debug("Calling operator: $urlString with subset: $subset namespace=$namespace")

        val response =
            try {
                client.get(urlString) {
                    subset?.let { headers.append(SUBSET, it) }
                    namespace?.let { headers.append(NAMESPACE, it) }
                }
            } catch (e: Exception) {
                log.error("Exception from Operator with url $urlString is $e")
                throw InternalServerErrorException("Operator responded with an error")
            }

        log.debug("Operator Response status: ${response.status.value}")

        when {
            response.status.value == 404 -> {
                log.warn("No routing found for tenant: $tenantId, channel: $channelId (HTTP 404)")
                throw NoRoutingInfoFoundException(msg = "No routing info found operator for tenant: $tenantId, channel: $channelId")
            }
            !response.status.isSuccess() -> {
                val errorBody = response.bodyAsText()
                log.error("Unexpected error from operator: ${response.status.value}, Body: $errorBody")
                throw UnexpectedResponseException("Unexpected response from operator")
            }
        }

        return try {
            response.body<ChannelRouting>()
        } catch (e: Exception) {
            log.error("Unexpected response body from operator: ${response.bodyAsText()}, exception: ${e.printStackTrace()}")
            throw UnexpectedResponseException("Unexpected response body from operator: ${e.message}")
        }
    }
}
