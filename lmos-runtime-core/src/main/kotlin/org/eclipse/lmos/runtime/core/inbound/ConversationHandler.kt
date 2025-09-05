/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.inbound

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.eclipse.lmos.runtime.core.LmosRuntimeConfig
import org.eclipse.lmos.runtime.core.cache.LmosRuntimeTenantAwareCache
import org.eclipse.lmos.runtime.core.constants.LmosRuntimeConstants.Cache.ROUTES
import org.eclipse.lmos.runtime.core.disambiguation.DisambiguationHandler
import org.eclipse.lmos.runtime.core.exception.AgentNotFoundException
import org.eclipse.lmos.runtime.core.model.Address
import org.eclipse.lmos.runtime.core.model.AssistantMessage
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.model.registry.RoutingInformation
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRoutingService
import org.slf4j.LoggerFactory

const val ACTIVE_FEATURES_KEY = "activeFeatures"
const val ACTIVE_FEATURE_KEY_CLASSIFIER = "classifier"

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
    private val agentClassifierService: AgentClassifierService,
    private val agentClientService: AgentClientService,
    private val lmosRuntimeConfig: LmosRuntimeConfig,
    private val lmosRuntimeTenantAwareCache: LmosRuntimeTenantAwareCache<RoutingInformation>,
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

            val agentName: String
            val agentAddress: Address
            // The feature flag is only needed during the transition period until
            // the old classifier is fully replaced by the new classifier.
            val activeFeatures = conversation.systemContext.contextParams.firstOrNull { it.key == ACTIVE_FEATURES_KEY }
            if (activeFeatures?.value?.contains(ACTIVE_FEATURE_KEY_CLASSIFIER) == true) {
                log.info("Classifier feature is active, using new classifier for agent routing")
                val classificationResult =
                    agentClassifierService.classify(
                        conversation,
                        routingInformation.agentList,
                        tenantId,
                        routingInformation.subset,
                    )
                if (classificationResult.agents.isEmpty()) {
                    if (disambiguationHandler != null) {
                        return@coroutineScope flow {
                            emit(disambiguationHandler.disambiguate(conversation, classificationResult.topRankedEmbeddings))
                        }
                    } else {
                        throw AgentNotFoundException("Failed to classify agent for conversationId '$conversationId'.")
                    }
                }
                val classifiedAgent = classificationResult.agents.first()
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
                ).onEach {
                    log.info("Agent Response: ${it.content}")
                }
        }
}
