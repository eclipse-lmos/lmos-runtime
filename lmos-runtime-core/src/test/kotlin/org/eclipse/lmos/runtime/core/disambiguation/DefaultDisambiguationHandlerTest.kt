/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.runtime.core.disambiguation

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.Capability
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.model.InputContext
import org.eclipse.lmos.runtime.core.model.SystemContext
import org.eclipse.lmos.runtime.core.model.UserContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DefaultDisambiguationHandlerTest {
    private val chatModel = mockk<ChatModel>()
    private val introductionPrompt = "Intro prompt"
    private val clarificationPrompt = "Clarify: {{topics}}"

    private val underTest =
        DefaultDisambiguationHandler(
            chatModel,
            introductionPrompt,
            clarificationPrompt,
        )

    @Test
    fun `disambiguate prepares chat model messages and returns clarification question correctly`() {
        // given
        val userMessage = "Hello, I need help with my contract"
        val conversation = conversation(userMessage)
        val candidateAgents = candidateAgents()
        val chatModelResponse = chatResponse(disambiguationJsonResponse())
        val messagesSlot = slot<List<ChatMessage>>()
        every { chatModel.chat(capture(messagesSlot)) } returns chatModelResponse

        // when
        val disambiguationResult = underTest.disambiguate(conversation, candidateAgents)

        // then ...
        // chat model messages were prepared correctly
        val messages = messagesSlot.captured
        assertEquals(3, messages.size)

        assertEquals(messages[0].javaClass, SystemMessage::class.java)
        assertEquals(introductionPrompt, (messages[0] as SystemMessage).text())

        assertEquals(messages[1].javaClass, UserMessage::class.java)
        assertEquals(userMessage, (messages[1] as UserMessage).singleText())

        assertEquals(messages[2].javaClass, SystemMessage::class.java)
        assertEquals(
            """
            Clarify: Topic 'contract-agent-id':
             - View contract details
             - Cancel a contract
            """.trimIndent(),
            (messages[2] as SystemMessage).text(),
        )
        // and clarification question is returned
        assertNotNull(disambiguationResult)
        assertEquals("Which contract would you like to view?", disambiguationResult.content)
    }

    @Test
    fun `disambiguate throws IllegalStateException when response is null`() {
        // given
        val conversation = conversation("Whats up?")
        val agents = candidateAgents()
        val chatResponse = chatResponse(null)
        every { chatModel.chat(any<List<ChatMessage>>()) } returns chatResponse

        // when
        val exception =
            assertThrows(IllegalStateException::class.java) {
                underTest.disambiguate(conversation, agents)
            }

        // then
        assertEquals("Disambiguation response is empty or null.", exception.message)
    }

    @Test
    fun `disambiguate throws IllegalArgumentException when JSON response is invalid`() {
        val conversation = conversation("Whats up?")
        val agents = candidateAgents()
        val chatResponse = chatResponse("invalid json")
        every { chatModel.chat(any<List<ChatMessage>>()) } returns chatResponse

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                underTest.disambiguate(conversation, agents)
            }

        assertTrue(exception.message!!.contains("Invalid disambiguation result format."))
    }

    private fun candidateAgents() =
        listOf(
            Agent(
                id = "contract-agent-id",
                name = "contract-agent",
                address = "http://contract-agent.example.com",
                capabilities =
                    listOf(
                        Capability("view-contract-id", "View contract details"),
                        Capability("cancel-contract-id", "Cancel a contract"),
                    ),
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
            .aiMessage(AiMessage(text, emptyList()))
            .build()

    private fun disambiguationJsonResponse() =
        """
        {
            "topics": ["contract"],
            "scratchpad": "notes",
            "onlyConfirmation": false,
            "confidence": 100,
            "clarificationQuestion": "Which contract would you like to view?"
        }
        """.trimIndent()
}
