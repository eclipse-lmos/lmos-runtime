/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.runtime.core.disambiguation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.runtime.core.model.AssistantMessage
import org.eclipse.lmos.runtime.core.model.Conversation
import org.slf4j.LoggerFactory

/**
 * The [DisambiguationHandler] can be used to generate disambiguation messages in order to assist the user
 * when no agents could be found to address their concerns. It uses the top-ranked agent candidates to
 * construct the assistant message.
 */
interface DisambiguationHandler {
    /**
     * Generates a disambiguation message based on the conversation
     * and the given top-ranked agent candidates.
     *
     * @param conversation current conversation
     * @param candidateAgents agents with the highest match scores
     * @return assistant message to be sent to the user
     */
    fun disambiguate(
        conversation: Conversation,
        candidateAgents: List<Agent>,
    ): AssistantMessage
}

class DefaultDisambiguationHandler(
    private val chatModel: ChatModel,
    private val introductionPrompt: String,
    private val clarificationPrompt: String,
) : DisambiguationHandler {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val jacksonObjectMapper = jacksonObjectMapper()

    override fun disambiguate(
        conversation: Conversation,
        candidateAgents: List<Agent>,
    ): AssistantMessage {
        val disambiguationMessages = mutableListOf<ChatMessage>()
        disambiguationMessages.add(prepareIntroductionSystemMessage())
        disambiguationMessages.addAll(prepareChatMessages(conversation))
        disambiguationMessages.add(prepareClarificationSystemMessage(candidateAgents))

        val chatResponse = chatModel.chat(disambiguationMessages)
        val disambiguationResult = prepareDisambiguationResult(chatResponse)
        logger.info("Disambiguation result: $disambiguationResult")

        return AssistantMessage(
            content = disambiguationResult.clarificationQuestion,
            anonymizationEntities = emptyList(),
        )
    }

    private fun prepareIntroductionSystemMessage() = SystemMessage(introductionPrompt)

    private fun prepareClarificationSystemMessage(agents: List<Agent>) =
        SystemMessage(
            clarificationPrompt.replace(
                "{{topics}}",
                agents.joinToString("\n\n") { agent ->
                    String.format(
                        "Topic '%s':\n - %s",
                        agent.id,
                        agent.capabilities.joinToString("\n - ") { capability -> capability.description },
                    )
                },
            ),
        )

    private fun prepareChatMessages(conversation: Conversation): List<ChatMessage> =
        conversation.inputContext.messages.mapNotNull {
            when (it.role) {
                "user" -> UserMessage(it.content)
                "assistant" -> AiMessage(it.content)
                else -> null
            }
        }

    private fun prepareDisambiguationResult(chatResponse: ChatResponse): DisambiguationResult {
        val json =
            chatResponse.aiMessage()?.text()
                ?: throw IllegalStateException("Disambiguation response is empty or null.")

        return try {
            jacksonObjectMapper.readValue(json, DisambiguationResult::class.java)
        } catch (ex: Exception) {
            logger.error("Failed to parse disambiguation result, JSON: $json", ex)
            throw IllegalArgumentException("Invalid disambiguation result format.", ex)
        }
    }
}

data class DisambiguationResult(
    val topics: List<String>,
    val scratchpad: String,
    val onlyConfirmation: Boolean,
    val confidence: Int,
    val clarificationQuestion: String,
)
