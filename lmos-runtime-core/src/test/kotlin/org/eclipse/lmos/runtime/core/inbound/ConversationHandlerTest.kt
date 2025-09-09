/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.inbound

import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.eclipse.lmos.runtime.core.constants.RuntimeConstants.Cache.ROUTES
import org.eclipse.lmos.runtime.core.disambiguation.DisambiguationHandler
import org.eclipse.lmos.runtime.core.exception.AgentClientException
import org.eclipse.lmos.runtime.core.exception.AgentNotFoundException
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.model.*
import org.eclipse.lmos.runtime.core.model.registry.RoutingInformation
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRoutingService
import org.eclipse.lmos.runtime.core.service.routing.ExplicitAgentRoutingService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ConversationHandlerTest {

    private lateinit var agentRegistryService: AgentRegistryService
    private lateinit var agentRoutingService: AgentRoutingService
    private lateinit var agentClassifierService: AgentClassifierService
    private lateinit var agentClientService: AgentClientService
    private lateinit var channelRoutingRepository: ChannelRoutingRepository
    private lateinit var conversationHandler: ConversationHandler
    private lateinit var lmosRuntimeConfig: RuntimeConfiguration
    private lateinit var disambiguationHandler: DisambiguationHandler

    @BeforeEach
    fun setUp() {
        agentClientService = mockk<AgentClientService>()
        agentRegistryService = mockk<AgentRegistryService>()

        agentRoutingService = ExplicitAgentRoutingService()
        agentClassifierService = mockk<AgentClassifierService>()
        channelRoutingRepository = mockk<ChannelRoutingRepository>()
        lmosRuntimeConfig =
            RuntimeConfiguration(
                mockk<RuntimeConfiguration.AgentRegistry>(),
                cache = RuntimeConfiguration.Cache(ttl = 6000),
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
    }

    @Test
    fun `test handleConversation with null subset`() =
        runBlocking {
            val conversationId = "testConversationId"
            val tenantId = "testTenantId"
            val turnId = "testTurnId"

            val conversation = conversation()
            val routingInformation = routingInformation()

            val resolvedAgent = routingInformation.agentList[0]
            val agentResponse = AssistantMessage("response")

            mockAgentRegistry(tenantId, conversation.systemContext.channelId, routingInformation)
            mockAgentClient(
                conversation,
                conversationId,
                turnId,
                resolvedAgent.name,
                Address(uri = "http://localhost:8080/"),
                null,
                agentResponse,
            )

            // Invoke method
            val result = conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, null).first()

            // Assertions
            assertEquals(agentResponse, result)

            // Verify that getRoutingInformation was called with null subset
            coVerify(exactly = 1) {
                agentRegistryService.getRoutingInformation(tenantId, conversation.systemContext.channelId, null)
            }
        }

    @Test
    fun `test handleConversation with non-null subset`() =
        runBlocking {
            val conversationId = "testConversationId"
            val tenantId = "testTenantId"
            val turnId = "testTurnId"
            val subset = "test-subset"

            val conversation = conversation()
            val routingInformation = routingInformation(subset)

            val resolvedAgent = routingInformation.agentList[0]
            val agentResponse = AssistantMessage("response")

            mockAgentRegistry(tenantId, conversation.systemContext.channelId, routingInformation)
            mockAgentClient(
                conversation,
                conversationId,
                turnId,
                resolvedAgent.name,
                Address(uri = "http://localhost:8080/"),
                subset,
                agentResponse,
            )

            // Invoke method
            val result = conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, subset).first()

            // Assertions
            assertEquals(agentResponse, result)

            // Verify that getRoutingInformation was called with the correct subset
            coVerify(exactly = 1) {
                agentRegistryService.getRoutingInformation(tenantId, conversation.systemContext.channelId, subset)
            }
        }

    @Test
    fun `test subset returned by routing information cached and used in agent call`() =
        runBlocking {
            val conversationId = "conv1"
            val tenantId = "tenant1"
            val turnId = "turn1"
            val subset = "non-null-subset"

            val conversation = conversation()
            val routingInformation = routingInformation(subset)

            val resolvedAgent = routingInformation.agentList[0]
            val assistantMessage = AssistantMessage("Response from agent", listOf())

            mockAgentRegistry(tenantId, conversation.systemContext.channelId, routingInformation)
            mockAgentClient(
                conversation,
                conversationId,
                turnId,
                resolvedAgent.name,
                resolvedAgent.addresses.first(),
                subset,
                assistantMessage,
            )

            // Execute the method
            conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, null).first()

            coVerify {
                agentClientService.askAgent(
                    conversation,
                    conversationId,
                    turnId,
                    resolvedAgent.name,
                    resolvedAgent.addresses.first(),
                    subset,
                )
            }
        }

    @Test
    fun `test routing information is cached`() =
        runBlocking {
            // Arrange
            val conversationId = "conv-124"
            val tenantId = "tenant-1"
            val turnId = "turn-1"
            val cachedSubset = "cached-subset"

            val conversation = conversation()
            val routingInformation = routingInformation(cachedSubset)

            val resolvedAgent = routingInformation.agentList[0]
            val expectedAgentResponse = AssistantMessage(content = "Test response")

            mockAgentRegistry(tenantId, conversation.systemContext.channelId, routingInformation)
            mockAgentClient(
                conversation,
                conversationId,
                turnId,
                resolvedAgent.name,
                resolvedAgent.addresses.first(),
                cachedSubset,
                expectedAgentResponse,
            )

            val result =
                conversationHandler
                    .handleConversation(
                        conversation,
                        conversationId,
                        tenantId,
                        turnId,
                        null,
                    ).first()

            assertEquals(expectedAgentResponse, result)

            coVerify(exactly = 1) {
                lmosRuntimeTenantAwareCache.save(
                    tenantId,
                    ROUTES,
                    conversationId,
                    routingInformation,
                    any(),
                )
            }
        }

    @Test
    fun `test cached routing information is used`() =
        runBlocking {
            // Arrange
            val conversationId = "conv-124"
            val tenantId = "tenant-1"
            val turnId = "turn-1"
            val cachedSubset = "cached-subset"

            val conversation = conversation()
            val routingInformation = routingInformation(cachedSubset)

            val resolvedAgent = routingInformation.agentList[0]
            val expectedAgentResponse = AssistantMessage(content = "Test response")

            lmosRuntimeTenantAwareCache.save(tenantId, ROUTES, conversationId, routingInformation)
            clearAllMocks()

            mockAgentRegistry(tenantId, conversation.systemContext.channelId, routingInformation)
            mockAgentClient(
                conversation,
                conversationId,
                turnId,
                resolvedAgent.name,
                resolvedAgent.addresses.first(),
                cachedSubset,
                expectedAgentResponse,
            )

            val result =
                conversationHandler
                    .handleConversation(
                        conversation,
                        conversationId,
                        tenantId,
                        turnId,
                        null,
                    ).first()

            assertEquals(expectedAgentResponse, result)

            coVerify(exactly = 0) {
                lmosRuntimeTenantAwareCache.save(
                    tenantId,
                    ROUTES,
                    conversationId,
                    routingInformation,
                )
            }

            // Verify that getRoutingInformation was not called
            coVerify(exactly = 0) {
                agentRegistryService.getRoutingInformation(any(), any(), any())
            }
        }

    @Test
    fun `test different subset parameter should not overrides cached routing information`() =
        runBlocking {
            // Arrange
            val conversationId = "conv-125"
            val tenantId = "tenant-1"
            val turnId = "turn-1"
            val cachedSubset = "cached-subset"
            val newSubset = "new-subset"

            val conversation = conversation()
            val cachedRoutingInformation = routingInformation(cachedSubset)

            val resolvedAgent = cachedRoutingInformation.agentList[0]
            val expectedAgentResponse = AssistantMessage(content = "Test response with new subset")

            // Save the cached routing information
            lmosRuntimeTenantAwareCache.save(tenantId, ROUTES, conversationId, cachedRoutingInformation)
            clearAllMocks()

            mockAgentClient(
                conversation,
                conversationId,
                turnId,
                resolvedAgent.name,
                resolvedAgent.addresses.first(),
                cachedSubset,
                expectedAgentResponse,
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

            // Verify that getRoutingInformation was called with the new subset
            coVerify(exactly = 0) {
                agentRegistryService.getRoutingInformation(tenantId, conversation.systemContext.channelId, any())
            }

            // Verify that the new routing information was cached
            coVerify(exactly = 0) {
                lmosRuntimeTenantAwareCache.save(
                    tenantId,
                    ROUTES,
                    conversationId,
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun `when agent registry returns error then throws exception`() {
        // Setup
        val conversation = conversation()
        val conversationId = "testConversationId"
        val tenantId = "testTenantId"
        val turnId = "testTurnId"

        coEvery {
            agentRegistryService.getRoutingInformation(tenantId, conversation.systemContext.channelId, any())
        } throws NoRoutingInfoFoundException("Registry Error")

        assertThrows<NoRoutingInfoFoundException> {
            runBlocking {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, null).first()
            }
        }
    }

    @Test
    fun `when agent client returns error then throws exception`() {
        // Setup
        val conversationId = "testConversationId"
        val tenantId = "testTenantId"
        val turnId = "testTurnId"

        val conversation = conversation()
        val routingInformation = routingInformation()

        mockAgentRegistry(tenantId, conversation.systemContext.channelId, routingInformation)
        coEvery {
            agentClientService.askAgent(conversation, conversationId, turnId, any(), any(), null)
        } throws AgentClientException("Agent Communication Error")

        assertThrows<AgentClientException> {
            runBlocking {
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, null).first()
            }
        }
    }

    @Test
    fun `handleConversation should successfully route and get agent response`() =
        runBlocking {
            // Arrange
            val conversationId = "conv-123"
            val tenantId = "tenant-1"
            val turnId = "turn-1"
            val subset = "subset-1"

            val conversation = conversation()
            val routingInformation = routingInformation(subset)
            val resolvedAgent = routingInformation.agentList[0]

            val expectedAgentResponse = AssistantMessage(content = "Test response")

            mockAgentRegistry(tenantId, conversation.systemContext.channelId, routingInformation)
            mockAgentClient(
                conversation,
                conversationId,
                turnId,
                resolvedAgent.name,
                resolvedAgent.addresses.first(),
                subset,
                expectedAgentResponse,
            )

            // Act
            val result =
                conversationHandler
                    .handleConversation(
                        conversation,
                        conversationId,
                        tenantId,
                        turnId,
                        null,
                    ).first()

            // Assert
            assertEquals(expectedAgentResponse, result)
        }

    @Test
    fun `disambiguation is executed when disambiguation is activated`() =
        runBlocking {
            // given
            val conversationId = "conv-124"
            val tenantId = "tenant-1"
            val turnId = "turn-1"
            val conversation = conversation(listOf(KeyValuePair(ACTIVE_FEATURES_KEY, ACTIVE_FEATURE_KEY_CLASSIFIER)))
            val routingInformation = routingInformation()
            val expectedDisambiguationResponse = AssistantMessage(content = "Please give me more details.")

            mockAgentRegistry(tenantId, conversation.systemContext.channelId, routingInformation)
            mockAgentClassifierService(conversation, routingInformation.agentList, tenantId, ClassificationResult(emptyList(), emptyList()))
            mockDisambiguationHandler(conversation, emptyList(), expectedDisambiguationResponse)

            // when
            val result = conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, null).first()

            // then
            assertEquals(expectedDisambiguationResponse, result)

            coVerify(exactly = 1) {
                disambiguationHandler.disambiguate(conversation, any())
            }
        }

    @Test
    fun `AgentNotFoundException is thrown when disambiguation is deactivated`() =
        runBlocking {
            // given
            val conversationId = "conv-124"
            val tenantId = "tenant-1"
            val turnId = "turn-1"
            val conversation = conversation(listOf(KeyValuePair(ACTIVE_FEATURES_KEY, ACTIVE_FEATURE_KEY_CLASSIFIER)))
            val routingInformation = routingInformation()

            mockAgentRegistry(tenantId, conversation.systemContext.channelId, routingInformation)
            mockAgentClassifierService(conversation, routingInformation.agentList, tenantId, ClassificationResult(emptyList(), emptyList()))

            val conversationHandler =
                DefaultConversationHandler(
                    agentRegistryService,
                    agentRoutingService,
                    agentClassifierService,
                    agentClientService,
                    lmosRuntimeConfig,
                    lmosRuntimeTenantAwareCache,
                    null, // Disambiguation handler is not provided
                )

            // then
            assertThrows<AgentNotFoundException> {
                runBlocking {
                    conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, null).first()
                }
            }

            coVerify(exactly = 0) {
                disambiguationHandler.disambiguate(conversation, any())
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

    private fun routingInformation(subset: String? = null): RoutingInformation {
        val routingInformation =
            RoutingInformation(
                agentList = listOf(Agent("agent1Id", "agent1", "v1", "desc", listOf(), listOf(Address(uri = "http://localhost:8080/")))),
                subset = subset,
            )
        return routingInformation
    }

    private fun mockAgentClient(
        conversation: Conversation,
        conversationId: String,
        turnId: String,
        agentName: String,
        address: Address,
        subset: String?,
        agentResponse: AssistantMessage,
    ) {
        coEvery {
            agentClientService.askAgent(conversation, conversationId, turnId, agentName, address, subset)
        } returns flow { emit(agentResponse) }
    }

    private fun mockAgentRegistry(
        tenantId: String,
        channelId: String,
        routingInformation: RoutingInformation,
    ) {
        coEvery { agentRegistryService.getRoutingInformation(tenantId, channelId, any()) } returns routingInformation
    }

    private fun mockAgentClassifierService(
        conversation: Conversation,
        agents: List<Agent>,
        tenantId: String,
        classificationResult: ClassificationResult,
    ) {
        coEvery { agentClassifierService.classify(conversation, agents, tenantId, null) } returns classificationResult
    }

    private fun mockDisambiguationHandler(
        conversation: Conversation,
        candidateAgents: List<org.eclipse.lmos.classifier.core.Agent>,
        assistantMessage: AssistantMessage,
    ) {
        coEvery { disambiguationHandler.disambiguate(conversation, candidateAgents) } returns assistantMessage
    }
}
