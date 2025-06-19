/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.outbound

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.eclipse.lmos.runtime.core.LmosRuntimeConfig
import org.eclipse.lmos.runtime.core.constants.LmosRuntimeConstants.SUBSET
import org.eclipse.lmos.runtime.core.exception.InternalServerErrorException
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.exception.UnexpectedResponseException
import org.eclipse.lmos.runtime.core.model.registry.ChannelRouting
import org.eclipse.lmos.runtime.core.model.registry.RoutingInformation
import org.eclipse.lmos.runtime.core.model.registry.toAgent
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.slf4j.LoggerFactory

class LmosOperatorAgentRegistry(
    private val lmosRuntimeConfig: LmosRuntimeConfig,
) : AgentRegistryService {
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

    private val log = LoggerFactory.getLogger(LmosOperatorAgentRegistry::class.java)

    @Override
    override suspend fun getRoutingInformation(
        tenantId: String,
        channelId: String,
        subset: String?,
    ): RoutingInformation {
        val urlString =
            "${lmosRuntimeConfig.agentRegistry.baseUrl}/apis/v1/tenants/$tenantId/channels/$channelId/routing"
        log.trace("Calling operator: $urlString with subset: $subset")

        val response =
            try {
                client.get(urlString) {
                    subset?.let { headers.append(SUBSET, it) }
                }
            } catch (e: Exception) {
                log.error("Exception from Operator with url $urlString is $e")
                throw InternalServerErrorException("Operator responded with an error")
            }

        log.info("Operator Response status: ${response.status.value}")

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
            response
                .bodyAsText()
                .let {
                    log.debug("Get agents from operator response: $it")
                    json.decodeFromString<ChannelRouting>(it)
                }.toRoutingInformation(response.headers[SUBSET])
        } catch (e: Exception) {
            log.error("Unexpected response body from operator: ${response.bodyAsText()}, exception: ${e.printStackTrace()}")
            throw UnexpectedResponseException("Unexpected response body from operator: ${e.message}")
        }
    }
}

private fun ChannelRouting.toRoutingInformation(subset: String?) = RoutingInformation(this.toAgent(), subset)
