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
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRoutingService
import org.eclipse.lmos.runtime.outbound.RoutingInformation
import org.slf4j.LoggerFactory

interface ConversationHandler {
    suspend fun handleConversation(
        conversation: Conversation,
        conversationId: String,
        tenantId: String,
        turnId: String,
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
    ): Flow<AssistantMessage> =
        coroutineScope {
            log.debug("Request Received, conversationId: $conversationId, turnId: $turnId")
            val routingInformation =
                lmosRuntimeTenantAwareCache.get(tenantId, ROUTES, conversationId)
                    ?: agentRegistryService
                        .getRoutingInformation(tenantId, conversation.systemContext.channelId)
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
                    log.info("Agent Response: $it")
                }
        }
}
