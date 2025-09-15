/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.channelrouting

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.constants.RuntimeConstants.SUBSET
import org.eclipse.lmos.runtime.core.exception.InternalServerErrorException
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.exception.UnexpectedResponseException
import org.eclipse.lmos.runtime.core.model.registry.ChannelRouting
import org.slf4j.LoggerFactory

interface ChannelRoutingRepository {
    fun getChannelRouting(
        conversationId: String,
        tenantId: String,
        channelId: String,
        subset: String?,
    ): ChannelRouting
}

class InMemoryChannelRoutingRepository(
    private val channelRoutings: List<ChannelRouting>,
) : ChannelRoutingRepository {
    override fun getChannelRouting(
        conversationId: String,
        tenantId: String,
        channelId: String,
        subset: String?,
    ): ChannelRouting {
        val channelRouting =
            channelRoutings.firstOrNull { channelRouting ->
                channelRouting.metadata.labels.tenant == tenantId &&
                    channelRouting.metadata.labels.channel == channelId &&
                    channelRouting.metadata.labels.subset == subset
            }
        if (channelRouting == null) {
            throw NoRoutingInfoFoundException("No routing info found for tenant: $tenantId, channel: $channelId, subset: $subset")
        }
        return channelRouting
    }
}

open class LmosOperatorChannelRoutingRepository(
    private val runtimeConfig: RuntimeConfiguration,
) : ChannelRoutingRepository {
    private val log = LoggerFactory.getLogger(LmosOperatorChannelRoutingRepository::class.java)

    @OptIn(ExperimentalSerializationApi::class)
    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
            decodeEnumsCaseInsensitive = true
        }

    private val client: HttpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    json = json,
                )
            }
        }

    /**
     * Blocking wrapper around the suspend version of getChannelRouting.
     * Useful for Spring Cache, which doesn't support suspend functions.
     */
    override fun getChannelRouting(
        conversationId: String,
        tenantId: String,
        channelId: String,
        subset: String?,
    ): ChannelRouting =
        runBlocking {
            getChannelRoutingSuspend(tenantId, channelId, subset)
        }

    suspend fun getChannelRoutingSuspend(
        tenantId: String,
        channelId: String,
        subset: String?,
    ): ChannelRouting {
        val urlString =
            "${runtimeConfig.agentRegistry.baseUrl}/apis/v1/tenants/$tenantId/channels/$channelId/routing"
        log.debug("Calling operator: $urlString with subset: $subset")

        val response =
            try {
                client.get(urlString) {
                    subset?.let { headers.append(SUBSET, it) }
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
