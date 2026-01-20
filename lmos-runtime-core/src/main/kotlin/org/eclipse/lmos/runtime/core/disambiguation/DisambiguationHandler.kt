/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.disambiguation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ResponseFormatType
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.service.output.JsonSchemas
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.llm.SystemPromptRenderer
import org.eclipse.lmos.classifier.core.tracing.ClassifierTracer
import org.eclipse.lmos.classifier.core.tracing.NoopClassifierTracer
import org.eclipse.lmos.classifier.llm.OpenInferenceTags
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
     * @return the disambiguation result
     */
    suspend fun disambiguate(
        tenant: String,
        conversation: Conversation,
        candidateAgents: List<Agent>,
    ): DisambiguationResult
}

class DefaultDisambiguationHandler(
    private val chatModel: ChatModel,
    private val introductionPrompt: String,
    private val clarificationPrompt: String,
    private val systemPromptRenderer: SystemPromptRenderer,
    private val tracer: ClassifierTracer = NoopClassifierTracer(),
) : DisambiguationHandler {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val jacksonObjectMapper = jacksonObjectMapper()
    private val responseFormat =
        ResponseFormat
            .builder()
            .type(ResponseFormatType.JSON)
            .jsonSchema(JsonSchemas.jsonSchemaFrom(DisambiguationResult::class.java).get())
            .build()

    override suspend fun disambiguate(
        tenant: String,
        conversation: Conversation,
        candidateAgents: List<Agent>,
    ): DisambiguationResult =
        tracer.withSpan("llm") { tags ->
            val chatRequest = prepareChatRequest(tenant, conversation, candidateAgents)
            val chatResponse = chatModel.chat(chatRequest)
            val disambiguationResult = prepareDisambiguationResult(chatResponse)
            OpenInferenceTags.applyModelTracingTags(tags, chatRequest, chatResponse)
            logger
                .atDebug()
                .addKeyValue("result", disambiguationResult)
                .addKeyValue("event", "DISAMBIGUATION_DONE")
                .log("Executed disambiguation.")

            disambiguationResult
        }

    private fun prepareChatRequest(
        tenant: String,
        conversation: Conversation,
        candidateAgents: List<Agent>,
    ): ChatRequest {
        val disambiguationMessages = mutableListOf<ChatMessage>()
        disambiguationMessages.add(prepareIntroductionSystemMessage(tenant))
        disambiguationMessages.addAll(prepareChatMessages(conversation))
        disambiguationMessages.add(prepareClarificationSystemMessage(tenant, candidateAgents))

        return ChatRequest
            .builder()
            .responseFormat(responseFormat)
            .messages(disambiguationMessages)
            .build()
    }

    private fun prepareIntroductionSystemMessage(tenant: String) =
        SystemMessage(
            systemPromptRenderer.render(
                introductionPrompt,
                mapOf("tenant" to tenant),
            ),
        )

    private fun prepareClarificationSystemMessage(
        tenant: String,
        agents: List<Agent>,
    ) = SystemMessage(
        systemPromptRenderer.render(
            clarificationPrompt,
            mapOf("agents" to agents, "tenant" to tenant),
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
            jacksonObjectMapper.readValue<DisambiguationResult>(json)
        } catch (ex: Exception) {
            logger.error("Failed to parse disambiguation result, JSON: $json", ex)
            throw IllegalArgumentException("Invalid disambiguation result format.", ex)
        }
    }
}

data class DisambiguationResult(
    val topics: List<String>?,
    val reasoning: String,
    val onlyConfirmation: Boolean,
    val confidence: Int?,
    val clarificationQuestion: String,
)
