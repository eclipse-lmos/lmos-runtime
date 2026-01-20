/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.disambiguation

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.output.TokenUsage
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.Capability
import org.eclipse.lmos.classifier.llm.MvelSystemPromptRenderer
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.model.InputContext
import org.eclipse.lmos.runtime.core.model.SystemContext
import org.eclipse.lmos.runtime.core.model.UserContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class DefaultDisambiguationHandlerTest {
    private val chatModel = mockk<ChatModel>()
    private val tenant = "de"
    private val introductionPrompt = "Intro prompt for tenant @{tenant}."
    private val expectedIntroductionPrompt = "Intro prompt for tenant $tenant."
    private val clarificationPrompt =
        "This is the clarification prompt, listing some agents: @foreach{agent : agents}@{agent.id}@end{', '} for tenant @{tenant}."
    private val expectedClarificationPrompt =
        "This is the clarification prompt, listing some agents: contract-agent, billing-agent for tenant $tenant."

    private val underTest =
        DefaultDisambiguationHandler(
            chatModel,
            introductionPrompt,
            clarificationPrompt,
            MvelSystemPromptRenderer(),
        )

    @Test
    fun `disambiguate prepares chat model messages and returns clarification question correctly`(): Unit =
        runBlocking {
            // given
            val userMessage = "Hello, I need help with my contract"
            val conversation = conversation(userMessage)
            val candidateAgents = candidateAgents()
            val chatModelResponse = chatResponse(disambiguationJsonResponse())
            val messagesSlot = slot<ChatRequest>()
            every { chatModel.chat(capture(messagesSlot)) } returns chatModelResponse

            // when
            val disambiguationResult = underTest.disambiguate(tenant, conversation, candidateAgents)

            // then ...
            // chat model messages were prepared correctly
            val messages = messagesSlot.captured.messages()
            assertEquals(3, messages.size)

            assertEquals(messages[0].javaClass, SystemMessage::class.java)
            assertEquals(expectedIntroductionPrompt, (messages[0] as SystemMessage).text())

            assertEquals(messages[1].javaClass, UserMessage::class.java)
            assertEquals(userMessage, (messages[1] as UserMessage).singleText())

            assertEquals(messages[2].javaClass, SystemMessage::class.java)
            assertEquals(expectedClarificationPrompt, (messages[2] as SystemMessage).text())
            // and clarification question is returned
            assertNotNull(disambiguationResult)
            assertEquals("Which contract would you like to view?", disambiguationResult.clarificationQuestion)
        }

    @Test
    fun `disambiguate throws IllegalStateException when response is null`(): Unit =
        runBlocking {
            // given
            val conversation = conversation("Whats up?")
            val agents = candidateAgents()
            val chatResponse = chatResponse(null)
            every { chatModel.chat(any<ChatRequest>()) } returns chatResponse

            // when
            val exception =
                assertFailsWith<IllegalStateException> {
                    underTest.disambiguate(tenant, conversation, agents)
                }

            // then
            assertEquals("Disambiguation response is empty or null.", exception.message)
        }

    @Test
    fun `disambiguate throws IllegalArgumentException when JSON response is invalid`(): Unit =
        runBlocking {
            val conversation = conversation("Whats up?")
            val agents = candidateAgents()
            val chatResponse = chatResponse("invalid json")
            every { chatModel.chat(any<ChatRequest>()) } returns chatResponse

            val exception =
                assertFailsWith<IllegalArgumentException> {
                    underTest.disambiguate(tenant, conversation, agents)
                }

            assertTrue(exception.message!!.contains("Invalid disambiguation result format."))
        }

    private fun candidateAgents() =
        listOf(
            Agent(
                id = "contract-agent",
                name = "contract-agent-name",
                address = "contract-agent-address",
                capabilities = listOf(Capability(id = "view-contract", description = "View contract details")),
            ),
            Agent(
                id = "billing-agent",
                name = "billing-agent-name",
                address = "billing-agent-address",
                capabilities = listOf(Capability(id = "view-bill", description = "View bill.")),
            ),
        )

    private fun conversation(userMessage: String) =
        Conversation(
            inputContext =
                InputContext(
                    messages = listOf(Message("user", userMessage)),
                ),
            systemContext = SystemContext(channelId = "channel1"),
            userContext = UserContext(userId = "user1", userToken = "token1"),
        )

    private fun chatResponse(text: String?): ChatResponse? =
        ChatResponse
            .builder()
            .modelName("MyModel")
            .tokenUsage(TokenUsage(1, 2, 3))
            .aiMessage(AiMessage(text, emptyList()))
            .build()

    private fun disambiguationJsonResponse() =
        """
        {
            "topics": ["contract"],
            "reasoning": "notes",
            "onlyConfirmation": false,
            "confidence": 100,
            "clarificationQuestion": "Which contract would you like to view?"
        }
        """.trimIndent()
}
