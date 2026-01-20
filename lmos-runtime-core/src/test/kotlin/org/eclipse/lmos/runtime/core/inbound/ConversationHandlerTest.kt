/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.inbound

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.channelrouting.*
import org.eclipse.lmos.runtime.core.disambiguation.DisambiguationHandler
import org.eclipse.lmos.runtime.core.disambiguation.DisambiguationResult
import org.eclipse.lmos.runtime.core.exception.AgentClientException
import org.eclipse.lmos.runtime.core.exception.AgentNotFoundException
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.model.*
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRoutingService
import org.eclipse.lmos.runtime.core.service.routing.ExplicitAgentRoutingService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ConversationHandlerTest {
    private lateinit var agentRoutingService: AgentRoutingService
    private lateinit var agentClassifierService: AgentClassifierService
    private lateinit var agentClientService: AgentClientService
    private lateinit var channelRoutingRepository: CachedChannelRoutingRepository
    private lateinit var conversationHandler: ConversationHandler
    private lateinit var lmosRuntimeConfig: RuntimeConfiguration
    private lateinit var disambiguationHandler: DisambiguationHandler
    private lateinit var channelRouting: ChannelRouting

    @BeforeEach
    fun setUp() {
        agentClientService = mockk<AgentClientService>()
        agentRoutingService = ExplicitAgentRoutingService()
        agentClassifierService = mockk<AgentClassifierService>()
        channelRoutingRepository = mockk<CachedChannelRoutingRepository>()
        lmosRuntimeConfig =
            RuntimeConfiguration(
                mockk<RuntimeConfiguration.ChannelRoutingRepositoryConfig>(),
                disambiguation =
                    RuntimeConfiguration.Disambiguation(
                        enabled = false,
                        llm =
                            RuntimeConfiguration.ChatModel(
                                provider = "openai",
                                model = "some-model",
                            ),
                    ),
            )
        disambiguationHandler = mockk<DisambiguationHandler>()
        conversationHandler =
            DefaultConversationHandler(
                agentRoutingService,
                agentClassifierService,
                channelRoutingRepository,
                agentClientService,
                disambiguationHandler,
            )

        channelRouting =
            ChannelRouting(
                metadata =
                    Metadata(
                        name = "dummy-name",
                        namespace = "dummy-namespace",
                        labels =
                            Labels(
                                channel = "web",
                                subset = "stable",
                                tenant = "de",
                                version = "1.0.0",
                            ),
                        creationTimestamp = "2024-01-01T00:00:00Z",
                        generation = 1,
                        resourceVersion = "1",
                        uid = "uid-123",
                    ),
                spec =
                    Spec(
                        capabilityGroups =
                            listOf(
                                CapabilityGroup(
                                    id = "agent1",
                                    name = "agent1",
                                    description = "First Agent",
                                    capabilities =
                                        listOf(
                                            Capability(
                                                id = "cap1",
                                                name = "cap1",
                                                providedVersion = "1.0.0",
                                                description = "Capability 1",
                                                host = "http://localhost:8080",
                                            ),
                                        ),
                                ),
                            ),
                    ),
            )

        // ChannelRoutingRepository Mock: Standardverhalten
        coEvery { channelRoutingRepository.getChannelRouting(any(), "de", any(), any(), any()) } returns channelRouting
    }

    @Test
    fun `handleConversation with non-null subset should use subset from channel routing`() =
        runTest {
            val conversationId = "testConversationId"
            val tenantId = "de"
            val turnId = "testTurnId"
            val subset = "test-subset"

            val conversation = conversation()
            val agentResponse = AssistantMessage("response")

            mockAgentClient(
                conversation,
                conversationId,
                turnId,
                agentResponse,
            )

            // Invoke method
            val result = conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, subset).first()

            // Assertions
            assertEquals(agentResponse, result)
        }

    @Test
    fun `test different subset parameter should not overrides cached routing information`() =
        runTest {
            // Arrange
            val conversationId = "conv-125"
            val tenantId = "de"
            val turnId = "turn-1"
            val newSubset = "new-subset"

            val conversation = conversation()

            val expectedAgentResponse = AssistantMessage(content = "Test response with new subset")

            mockAgentClient(
                conversation,
                conversationId,
                turnId,
                expectedAgentResponse,
                "stable", // Original subset from cached routing
            )

            // Call with a different subset parameter
            val result =
                conversationHandler
                    .handleConversation(
                        conversation,
                        conversationId,
                        tenantId,
                        turnId,
                        newSubset,
                    ).first()

            assertEquals(expectedAgentResponse, result)
        }

    @Test
    fun `when agent registry returns error then throws exception`() {
        // Setup
        val conversation = conversation()
        val conversationId = "testConversationId"
        val tenantId = "fail"
        val turnId = "testTurnId"

        coEvery {
            channelRoutingRepository.getChannelRouting(any(), "fail", any(), any(), any())
        } throws NoRoutingInfoFoundException("Registry Error")

        assertThrows<NoRoutingInfoFoundException> {
            runTest {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, "stable").first()
            }
        }
    }

    @Test
    fun `when agent client returns error then throws exception`() {
        // Setup
        val conversationId = "testConversationId"
        val tenantId = "de"
        val turnId = "testTurnId"

        val conversation = conversation()

        coEvery {
            agentClientService.askAgent(conversation, conversationId, turnId, any(), any(), any())
        } throws AgentClientException("Agent Communication Error")

        assertThrows<AgentClientException> {
            runTest {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, "stable").first()
            }
        }
    }

    @Test
    fun `disambiguation is executed when disambiguation is activated`() =
        runTest {
            // given
            val conversationId = "conv-124"
            val tenantId = "de"
            val turnId = "turn-1"
            val conversation = conversation(listOf(KeyValuePair(ACTIVE_FEATURES_KEY, ACTIVE_FEATURE_KEY_CLASSIFIER)))
            val expectedDisambiguationResult =
                DisambiguationResult(
                    topics = listOf("myTopics"),
                    reasoning = "some reasoning",
                    onlyConfirmation = false,
                    confidence = 85,
                    clarificationQuestion = "Please give me more details.",
                )

            mockAgentClassifierService(
                conversationId,
                conversation,
                tenantId,
                ClassificationResult(emptyList(), emptyList()),
            )
            mockDisambiguationHandler(conversation, tenantId, emptyList(), expectedDisambiguationResult)

            // when
            val result = conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, null).first()

            // then
            assertEquals(expectedDisambiguationResult.clarificationQuestion, result.content)

            coVerify(exactly = 1) {
                disambiguationHandler.disambiguate(tenantId, conversation, any())
            }
        }

    @Test
    fun `AgentNotFoundException is thrown when disambiguation is deactivated`() =
        runTest {
            // given
            val conversationId = "conv-124"
            val tenantId = "de"
            val turnId = "turn-1"
            val conversation = conversation(listOf(KeyValuePair(ACTIVE_FEATURES_KEY, ACTIVE_FEATURE_KEY_CLASSIFIER)))

            mockAgentClassifierService(
                conversationId,
                conversation,
                tenantId,
                ClassificationResult(emptyList(), emptyList()),
            )

            val conversationHandler =
                DefaultConversationHandler(
                    agentRoutingService,
                    agentClassifierService,
                    channelRoutingRepository,
                    agentClientService,
                    null, // Disambiguation handler is not provided
                )

            // then
            assertThrows<AgentNotFoundException> {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, null).first()
            }

            coVerify(exactly = 0) {
                disambiguationHandler.disambiguate(tenantId, conversation, any())
            }
        }

    private fun conversation(contextParams: List<KeyValuePair> = emptyList()): Conversation {
        val conversation =
            Conversation(
                inputContext =
                    InputContext(
                        messages = listOf(Message("user", "Hello")),
                        explicitAgent = "agent1",
                    ),
                systemContext =
                    SystemContext(
                        channelId = "channel1",
                        contextParams = contextParams,
                    ),
                userContext = UserContext(userId = "user1", userToken = "token1"),
            )
        return conversation
    }

    private fun mockAgentClient(
        conversation: Conversation,
        conversationId: String,
        turnId: String,
        agentResponse: AssistantMessage,
        subset: String = "stable",
    ) {
        coEvery {
            agentClientService.askAgent(conversation, conversationId, turnId, any(), any(), subset)
        } returns flow { emit(agentResponse) }
    }

    private fun mockAgentClassifierService(
        conversationId: String,
        conversation: Conversation,
        tenantId: String,
        classificationResult: ClassificationResult,
    ) {
        coEvery { agentClassifierService.classify(conversationId, conversation, tenantId, any()) } returns classificationResult
    }

    private fun mockDisambiguationHandler(
        conversation: Conversation,
        tenantId: String,
        candidateAgents: List<org.eclipse.lmos.classifier.core.Agent>,
        disambiguationResult: DisambiguationResult,
    ) {
        coEvery { disambiguationHandler.disambiguate(tenantId, conversation, candidateAgents) } returns disambiguationResult
    }
}
