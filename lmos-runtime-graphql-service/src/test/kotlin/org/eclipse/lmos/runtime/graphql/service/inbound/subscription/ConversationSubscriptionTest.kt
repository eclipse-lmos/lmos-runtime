package org.eclipse.lmos.runtime.graphql.service.inbound.subscription

import io.mockk.*
import kotlinx.coroutines.flow.*
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
            val results = resultFlow.toList()

            assertEquals(2, results.size)
            assertEquals(assistantMessage1, results[0])
            assertEquals(assistantMessage2, results[1])

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
            val results = resultFlow.toList()

            assertEquals(0, results.size)

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

            val exception = RuntimeException("Test exception")

            coEvery {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId)
            } throws exception

            val resultFlow = conversationSubscription.chat(conversation, conversationId, tenantId, turnId)

            val thrownException =
                assertThrows<RuntimeException> {
                    resultFlow.collect()
                }

            assertEquals(exception.message, thrownException.message)

            coVerify(exactly = 1) {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId)
            }
        }

    private fun conversation(): Conversation {
        val conversation =
            Conversation(
                inputContext =
                    InputContext(
                        messages = listOf(Message("user", "Hello")),
                        explicitAgent = "agent1",
                    ),
                systemContext = SystemContext(channelId = "channel1"),
                userContext = UserContext(userId = "user1", userToken = "token1"),
            )
        return conversation
    }
}
