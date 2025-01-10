/*
 * // SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.graphql.service.inbound.subscription

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.runtime.core.inbound.ConversationHandler
import org.eclipse.lmos.runtime.core.model.*
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

class ConversationSubscriptionTest {
    private lateinit var conversationHandler: ConversationHandler
    private lateinit var conversationSubscription: ConversationSubscription

    @BeforeEach
    fun setUp() {
        conversationHandler = mockk()
        conversationSubscription = ConversationSubscription(conversationHandler)
    }

    @Test
    fun `chat should return flow of assistant messages`() =
        runTest {
            val conversation = conversation()
            val conversationId = "conv123"
            val tenantId = "tenant456"
            val turnId = "turn789"

            val assistantMessage1 = AssistantMessage(content = "Hello, how can I assist you?")
            val assistantMessage2 = AssistantMessage(content = "Do you need help with anything else?")
            val assistantMessageFlow = flowOf(assistantMessage1, assistantMessage2)

            coEvery {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId)
            } returns assistantMessageFlow

            val resultFlow = conversationSubscription.chat(conversation, conversationId, tenantId, turnId)

            resultFlow.test {
                assertEquals(assistantMessage1, awaitItem())
                assertEquals(assistantMessage2, awaitItem())
                awaitComplete()
            }

            coVerify(exactly = 1) {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId)
            }
        }

    @Test
    fun `chat should handle empty flow of assistant messages`() =
        runTest {
            val conversation = conversation()
            val conversationId = "convEmpty"
            val tenantId = "tenantEmpty"
            val turnId = "turnEmpty"

            val assistantMessageFlow = emptyFlow<AssistantMessage>()

            coEvery {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId)
            } returns assistantMessageFlow

            val resultFlow = conversationSubscription.chat(conversation, conversationId, tenantId, turnId)

            resultFlow.test {
                awaitComplete()
            }

            coVerify(exactly = 1) {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId)
            }
        }

    @Test
    fun `chat should propagate exception from conversation handler`() =
        runTest {
            val conversation = conversation()
            val conversationId = "convError"
            val tenantId = "tenantError"
            val turnId = "turnError"

            val exceptionMessage = "Test exception"
            val exception = RuntimeException(exceptionMessage)

            coEvery {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId)
            } throws exception

            val resultFlow = conversationSubscription.chat(conversation, conversationId, tenantId, turnId)

            resultFlow.test {
                assertEquals(exceptionMessage, awaitError().message)
            }

            coVerify(exactly = 1) {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId)
            }
        }

    private fun conversation() =
        Conversation(
            inputContext =
                InputContext(
                    messages = listOf(Message("user", "Hello")),
                    explicitAgent = "agent1",
                ),
            systemContext = SystemContext(channelId = "channel1"),
            userContext = UserContext(userId = "user1", userToken = "token1"),
        )
}
