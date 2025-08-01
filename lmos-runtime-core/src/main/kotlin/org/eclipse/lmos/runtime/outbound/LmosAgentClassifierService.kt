package org.eclipse.lmos.runtime.outbound

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.lmos.classifier.core.*
import org.eclipse.lmos.runtime.core.model.Agent
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService

class LmosAgentClassifierService(
    private val classifier: AgentClassifier,
) : AgentClassifierService {
    override suspend fun classify(
        conversation: Conversation,
        agents: List<Agent>,
        tenant: String,
    ): ClassificationResult {
        val (userMessage, historyMessages) = prepareMessages(conversation)
        val classifierAgents = prepareAgents(agents)
        return withContext(Dispatchers.IO) {
            classifier.classify(
                ClassificationRequest(
                    inputContext =
                        InputContext(
                            userMessage = userMessage,
                            historyMessages = historyMessages,
                            agents = classifierAgents,
                        ),
                    systemContext =
                        SystemContext(
                            tenantId = tenant,
                            channelId = conversation.systemContext.channelId,
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

    private fun prepareAgents(agents: List<Agent>): List<org.eclipse.lmos.classifier.core.Agent> =
        agents.map {
            Agent(
                it.id,
                it.name,
                it.addresses.random().uri,
                it.capabilities.map { cap ->
                    Capability(cap.id, cap.description, listOf())
                },
            )
        }
}
