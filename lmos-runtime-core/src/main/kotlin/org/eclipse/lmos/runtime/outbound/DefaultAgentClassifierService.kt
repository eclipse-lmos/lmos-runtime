/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.outbound

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.lmos.classifier.core.*
import org.eclipse.lmos.classifier.core.llm.AgentProvider
import org.eclipse.lmos.runtime.core.channelrouting.CachedChannelRoutingRepository
import org.eclipse.lmos.runtime.core.channelrouting.toRoutingInformation
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService

const val CONVERSATION_ID_METADATA_KEY = "conversationId"

class DefaultAgentClassifierService(
    private val classifier: AgentClassifier,
) : AgentClassifierService {
    override suspend fun classify(
        conversationId: String,
        conversation: Conversation,
        tenant: String,
        subset: String?,
    ): ClassificationResult {
        val (userMessage, historyMessages) = prepareMessages(conversation)
        return withContext(Dispatchers.IO) {
            classifier.classify(
                ClassificationRequest(
                    inputContext =
                        InputContext(
                            userMessage = userMessage,
                            historyMessages = historyMessages,
                            metadata = mapOf(CONVERSATION_ID_METADATA_KEY to conversationId),
                        ),
                    systemContext =
                        SystemContext(
                            tenantId = tenant,
                            channelId = conversation.systemContext.channelId,
                            subset = if (subset.isNullOrEmpty()) "stable" else subset,
                        ),
                ),
            )
        }
    }

    private fun prepareMessages(conversation: Conversation): Pair<String, List<HistoryMessage>> {
        val userMessage =
            conversation.inputContext.messages
                .last()
                .content

        val historyMessages =
            conversation.inputContext.messages.dropLast(1).mapNotNull {
                when (it.role) {
                    "user" -> HistoryMessage(HistoryMessageRole.USER, it.content)
                    "assistant" -> HistoryMessage(HistoryMessageRole.ASSISTANT, it.content)
                    else -> null
                }
            }

        return Pair(userMessage, historyMessages)
    }
}

/**
 * Used only by the LLM-based classifier to provide agents based on channel routing information.
 */
class ChannelRoutingAgentProvider(
    private val cachedChannelRoutingRepository: CachedChannelRoutingRepository,
) : AgentProvider {
    override suspend fun provide(request: ClassificationRequest): List<Agent> {
        val conversationId =
            request.inputContext.metadata[CONVERSATION_ID_METADATA_KEY]
                ?: throw IllegalStateException(
                    "Cannot retrieve channel routing due to missing conversation ID in Classification Request: $request",
                )

        val channelRouting =
            cachedChannelRoutingRepository.getChannelRouting(
                conversationId = conversationId.toString(),
                tenantId = request.systemContext.tenantId,
                channelId = request.systemContext.channelId,
                subset = request.systemContext.subset,
                namespace = null,
            )

        return channelRouting.toRoutingInformation().agentList.map {
            Agent(
                it.id.ifEmpty { it.name },
                it.name,
                it.addresses.random().uri,
                it.capabilities.map { cap ->
                    Capability(cap.id, cap.description, listOf())
                },
            )
        }
    }
}
