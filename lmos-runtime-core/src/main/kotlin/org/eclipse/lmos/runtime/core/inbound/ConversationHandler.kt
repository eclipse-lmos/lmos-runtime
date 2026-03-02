/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.inbound

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.eclipse.lmos.runtime.core.channelrouting.CachedChannelRoutingRepository
import org.eclipse.lmos.runtime.core.channelrouting.toRoutingInformation
import org.eclipse.lmos.runtime.core.disambiguation.DisambiguationHandler
import org.eclipse.lmos.runtime.core.exception.AgentNotFoundException
import org.eclipse.lmos.runtime.core.model.Address
import org.eclipse.lmos.runtime.core.model.AssistantMessage
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
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
    private val agentRoutingService: AgentRoutingService,
    private val agentClassifierService: AgentClassifierService?,
    private val cachedChannelRoutingRepository: CachedChannelRoutingRepository,
    private val agentClientService: AgentClientService,
    private val disambiguationHandler: DisambiguationHandler?,
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

            // Retrieve ChannelRouting from cached Repository
            val channelRouting =
                cachedChannelRoutingRepository.getChannelRouting(
                    conversationId = conversationId,
                    tenantId = tenantId,
                    channelId = conversation.systemContext.channelId,
                    subset = subset,
                    namespace = null,
                )
            val routingInformation = channelRouting.toRoutingInformation()
            log.debug("Using routingInformation: {}", routingInformation)

            val agentName: String
            val agentAddress: Address

            if (agentClassifierService != null) {
                log.info("Classifier feature is active, using new classifier for agent routing")
                val classificationResult =
                    agentClassifierService.classify(
                        conversationId,
                        conversation,
                        tenantId,
                        routingInformation.subset,
                    )
                if (classificationResult.classifiedAgents.isEmpty()) {
                    if (disambiguationHandler != null) {
                        return@coroutineScope flow {
                            val result = disambiguationHandler.disambiguate(tenantId, conversation, classificationResult.candidateAgents)
                            emit(AssistantMessage(content = result.clarificationQuestion, anonymizationEntities = emptyList()))
                        }
                    } else {
                        throw AgentNotFoundException("Failed to classify agent for conversationId '$conversationId'.")
                    }
                }
                val classifiedAgent = classificationResult.classifiedAgents.first()
                agentName = classifiedAgent.name
                agentAddress = Address(uri = classifiedAgent.address)
            } else {
                log.info("Classifier feature is not active, using old classifier for agent routing")
                val agent = agentRoutingService.resolveAgentForConversation(conversation, routingInformation.agentList)
                agentName = agent.name
                agentAddress = agent.addresses.random()
            }

            log.info("Resolved agent: '$agentName'")
            agentClientService
                .askAgent(
                    conversation,
                    conversationId,
                    turnId,
                    agentName,
                    agentAddress,
                    routingInformation.subset,
                )
        }
}
