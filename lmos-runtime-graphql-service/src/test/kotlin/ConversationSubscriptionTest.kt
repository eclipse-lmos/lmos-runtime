/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.graphql.service.inbound.subscription

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.eclipse.lmos.arc.api.*
import org.eclipse.lmos.runtime.core.inbound.ConversationHandler
import org.eclipse.lmos.runtime.core.model.AssistantMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
            val agentRequest = createAgentRequest()

            val assistantMessage1 = AssistantMessage(content = "Hello, how can I assist you?")
            val assistantMessage2 = AssistantMessage(content = "Do you need help with anything else?")
            val assistantMessageFlow = flowOf(assistantMessage1, assistantMessage2)

            coEvery {
                conversationHandler.handleConversation(any(), any(), any(), any())
            } returns assistantMessageFlow

            val resultFlow = conversationSubscription.agent(null, agentRequest)

            resultFlow.test {
                assertEquals(assistantMessage1.toAgentResult(), awaitItem())
                assertEquals(assistantMessage2.toAgentResult(), awaitItem())
                awaitComplete()
            }

            coVerify(exactly = 1) {
                conversationHandler.handleConversation(any(), any(), any(), any())
            }
        }

    @Test
    fun `chat should handle empty flow of assistant messages`() =
        runTest {
            val agentRequest = createAgentRequest()

            val assistantMessageFlow = emptyFlow<AssistantMessage>()

            coEvery {
                conversationHandler.handleConversation(any(), any(), any(), any())
            } returns assistantMessageFlow

            val resultFlow = conversationSubscription.agent(null, agentRequest)

            resultFlow.test {
                awaitComplete()
            }

            coVerify(exactly = 1) {
                conversationHandler.handleConversation(any(), any(), any(), any())
            }
        }

    @Test
    fun `chat should propagate exception from conversation handler`() =
        runTest {
            val agentRequest = createAgentRequest()

            val exceptionMessage = "Test exception"
            val exception = RuntimeException(exceptionMessage)

            coEvery {
                conversationHandler.handleConversation(any(), any(), any(), any())
            } throws exception

            val resultFlow = conversationSubscription.agent(null, agentRequest)

            resultFlow.test {
                assertEquals(exceptionMessage, awaitError().message)
            }

            coVerify(exactly = 1) {
                conversationHandler.handleConversation(any(), any(), any(), any())
            }
        }

    private fun createAgentRequest() =
        AgentRequest(
            messages = listOf(Message("user", "Hello world")),
            systemContext =
                listOf(
                    SystemContextEntry("tenantId", "tenant id"),
                    SystemContextEntry("channelId", "channel id"),
                ),
            conversationContext =
                ConversationContext(
                    conversationId = "conversation id",
                    turnId = "turn id",
                    anonymizationEntities =
                        listOf(
                            AnonymizationEntity("type", "value", "replacement"),
                        ),
                ),
            userContext =
                org.eclipse.lmos.arc.api.UserContext(
                    userId = "user id",
                    userToken = "user token",
                    profile = listOf(ProfileEntry("key", "value")),
                ),
        )
}

private fun AssistantMessage.toAgentResult() =
    AgentResult(
        messages =
            listOf(
                Message(
                    role = "assistant",
                    content = this.content,
                    turnId = "turn id",
                ),
            ),
    )
