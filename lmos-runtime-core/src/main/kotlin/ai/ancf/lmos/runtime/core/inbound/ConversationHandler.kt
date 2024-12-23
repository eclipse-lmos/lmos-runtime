/*
 * SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.runtime.core.inbound

import ai.ancf.lmos.runtime.core.LmosRuntimeConfig
import ai.ancf.lmos.runtime.core.cache.LmosRuntimeTenantAwareCache
import ai.ancf.lmos.runtime.core.constants.LmosRuntimeConstants.Cache.ROUTES
import ai.ancf.lmos.runtime.core.model.Agent
import ai.ancf.lmos.runtime.core.model.AssistantMessage
import ai.ancf.lmos.runtime.core.model.Conversation
import ai.ancf.lmos.runtime.core.service.outbound.AgentClientService
import ai.ancf.lmos.runtime.core.service.outbound.AgentRegistryService
import ai.ancf.lmos.runtime.core.service.outbound.AgentRoutingService
import ai.ancf.lmos.runtime.outbound.RoutingInformation
import org.slf4j.LoggerFactory

interface ConversationHandler {
    suspend fun handleConversation(
        conversation: Conversation,
        conversationId: String,
        tenantId: String,
        turnId: String,
    ): AssistantMessage
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
    ): AssistantMessage {
        log.debug("Request Received, conversationId: $conversationId, turnId: $turnId")
        val routingInformation =
            lmosRuntimeTenantAwareCache.get(tenantId, ROUTES, conversationId)
                ?: agentRegistryService.getRoutingInformation(tenantId, conversation.systemContext.channelId)
                    .also { result ->
                        log.debug("Caching routing information: {}", result)
                        lmosRuntimeTenantAwareCache.save(
                            tenantId, ROUTES, conversationId,
                            result, lmosRuntimeConfig.cache.ttl,
                        )
                    }
        log.info("routingInformation: $routingInformation")
        val agent: Agent = agentRoutingService.resolveAgentForConversation(conversation, routingInformation.agentList)
        log.info("Resolved agent: $agent")
        val agentResponse =
            agentClientService.askAgent(
                conversation,
                conversationId,
                turnId,
                agent.name,
                agent.addresses.random(),
                routingInformation.subset,
            )
        log.info("Agent Response: $agentResponse")
        return agentResponse
    }
}
