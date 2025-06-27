/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.inbound

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.eclipse.lmos.runtime.core.LmosRuntimeConfig
import org.eclipse.lmos.runtime.core.cache.LmosRuntimeTenantAwareCache
import org.eclipse.lmos.runtime.core.constants.LmosRuntimeConstants.Cache.ROUTES
import org.eclipse.lmos.runtime.core.model.Agent
import org.eclipse.lmos.runtime.core.model.AssistantMessage
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.model.registry.RoutingInformation
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRoutingService
import org.slf4j.LoggerFactory

interface ConversationHandler {
    suspend fun handleConversation(
        conversation: Conversation,
        conversationId: String,
        tenantId: String,
        turnId: String,
        subset: String? = null,
    ): Flow<AssistantMessage>
}

class DefaultConversationHandler(
    private val agentRegistryService: AgentRegistryService,
    private val agentRoutingService: AgentRoutingService,
    private val agentClientService: AgentClientService,
    private val lmosRuntimeConfig: LmosRuntimeConfig,
    private val lmosRuntimeTenantAwareCache: LmosRuntimeTenantAwareCache<RoutingInformation>,
) : ConversationHandler {
    private val log = LoggerFactory.getLogger(DefaultConversationHandler::class.java)

    override suspend fun handleConversation(
        conversation: Conversation,
        conversationId: String,
        tenantId: String,
        turnId: String,
        subset: String?,
    ): Flow<AssistantMessage> =
        coroutineScope {
            log.info("Request Received, conversationId: $conversationId, turnId: $turnId, subset: $subset")
            val cachedRoutingInformation = lmosRuntimeTenantAwareCache.get(tenantId, ROUTES, conversationId)
            val routingInformation =
                if (subset == null && cachedRoutingInformation?.subset == subset) {
                    agentRegistryService
                        .getRoutingInformation(tenantId, conversation.systemContext.channelId, subset)
                        .also { result ->
                            log.debug("Caching routing information with new subset: {}", result)
                            lmosRuntimeTenantAwareCache.save(
                                tenantId,
                                ROUTES,
                                conversationId,
                                result,
                                lmosRuntimeConfig.cache.ttl,
                            )
                        }
                } else {
                    val existingSubset = cachedRoutingInformation?.subset ?: subset
                    cachedRoutingInformation ?: agentRegistryService
                        .getRoutingInformation(tenantId, conversation.systemContext.channelId, existingSubset)
                        .also { result ->
                            log.debug("Caching routing information: {}", result)
                            lmosRuntimeTenantAwareCache.save(
                                tenantId,
                                ROUTES,
                                conversationId,
                                result,
                                lmosRuntimeConfig.cache.ttl,
                            )
                        }
                }
            log.info("routingInformation: $routingInformation")
            val agent: Agent = agentRoutingService.resolveAgentForConversation(conversation, routingInformation.agentList)
            log.info("Resolved agent: $agent")

            agentClientService
                .askAgent(
                    conversation,
                    conversationId,
                    turnId,
                    agent.name,
                    agent.addresses.random(),
                    routingInformation.subset,
                ).onEach {
                    log.info("Agent Response: ${it.content}")
                }
        }
}
