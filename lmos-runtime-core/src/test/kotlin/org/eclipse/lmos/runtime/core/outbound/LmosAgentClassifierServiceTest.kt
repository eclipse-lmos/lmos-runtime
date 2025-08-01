/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.outbound

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.classifier.core.*
import org.eclipse.lmos.runtime.core.model.*
import org.eclipse.lmos.runtime.core.model.Agent
import org.eclipse.lmos.runtime.core.model.InputContext
import org.eclipse.lmos.runtime.core.model.SystemContext
import org.eclipse.lmos.runtime.outbound.LmosAgentClassifierService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LmosAgentClassifierServiceTest {
    private val classifierMock = mockk<AgentClassifier>()
    private val underTest = LmosAgentClassifierService(classifierMock)

    private val defaultTenantId = "myTenantId"
    private val defaultChannelId = "myChannelId"
    private val defaultConversationMessages =
        listOf(
            Message("user", "Hi"),
            Message("assistant", "How can I help you?"),
            Message("user", "I want to view my bill."),
        )
    private val defaultInputContext = InputContext(defaultConversationMessages)
    private val defaultSystemContext = SystemContext(defaultChannelId)
    private val defaultUserContext = UserContext("user-1", null)
    private val defaultAgent =
        Agent(
            id = "myAgentId",
            name = "myAgent",
            description = "myDescription",
            version = "1.2.3",
            capabilities =
                listOf(
                    AgentCapability(
                        id = "myCapId",
                        name = "myCap",
                        version = "1.2.3",
                        description = "myDescription",
                    ),
                ),
            addresses = listOf(Address(uri = "http://my-agent:8080")),
        )

    @Test
    fun `service calls classifier correctly`() =
        runBlocking {
            // Given
            val conversation = Conversation(defaultInputContext, defaultSystemContext, defaultUserContext)

            val agents = listOf(defaultAgent)

            var capturedRequest: ClassificationRequest? = null
            every { classifierMock.classify(any()) } answers {
                capturedRequest = firstArg()
                ClassificationResult(emptyList())
            }

            // When
            underTest.classify(conversation, agents, defaultTenantId)

            // Then
            assertNotNull(capturedRequest)

            val userMessage = capturedRequest!!.inputContext.userMessage
            assertEquals(defaultConversationMessages.last().content, userMessage)

            val historyMessages = capturedRequest!!.inputContext.historyMessages
            assertEquals(
                defaultConversationMessages.dropLast(1).mapNotNull {
                    when (it.role) {
                        "user" -> HistoryMessage(HistoryMessageRole.USER, it.content)
                        "assistant" -> HistoryMessage(HistoryMessageRole.ASSISTANT, it.content)
                        else -> null
                    }
                },
                historyMessages,
            )

            val agent = capturedRequest!!.inputContext.agents.first()
            assertEquals(defaultAgent.id, agent.id)
            assertEquals(defaultAgent.capabilities.first().id, agent.capabilities.first().id)
            assertEquals(defaultAgent.capabilities.first().description, agent.capabilities.first().description)

            val tenantId = capturedRequest!!.systemContext.tenantId
            assertEquals(defaultTenantId, tenantId)

            val channelId = capturedRequest!!.systemContext.channelId
            assertEquals(defaultChannelId, channelId)
        }
}
