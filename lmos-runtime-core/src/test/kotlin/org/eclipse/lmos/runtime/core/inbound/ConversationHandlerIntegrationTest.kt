/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.inbound

import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.arc.agent.client.graphql.GraphQlAgentClient
import org.eclipse.lmos.arc.api.AgentRequest
import org.eclipse.lmos.arc.api.AgentResult
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.runtime.core.LmosRuntimeConfig
import org.eclipse.lmos.runtime.core.cache.TenantAwareInMemoryCache
import org.eclipse.lmos.runtime.core.exception.AgentClientException
import org.eclipse.lmos.runtime.core.exception.AgentNotFoundException
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.model.*
import org.eclipse.lmos.runtime.core.model.registry.RoutingInformation
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService
import org.eclipse.lmos.runtime.core.service.routing.ExplicitAgentRoutingService
import org.eclipse.lmos.runtime.outbound.ArcAgentClientService
import org.eclipse.lmos.runtime.outbound.LmosOperatorAgentRegistry
import org.eclipse.lmos.runtime.test.BaseWireMockTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ConversationHandlerIntegrationTest : BaseWireMockTest() {
    private val lmosRuntimeConfig =
        LmosRuntimeConfig(
            agentRegistry = LmosRuntimeConfig.AgentRegistry(baseUrl = "http://localhost:$mockPort/agentRegistry"),
            cache = LmosRuntimeConfig.Cache(ttl = 6000),
        )
    private val lmosRuntimeTenantAwareCache = TenantAwareInMemoryCache<RoutingInformation>()
    private val agentRegistryService = LmosOperatorAgentRegistry(lmosRuntimeConfig)
    private val agentRoutingService = ExplicitAgentRoutingService()
    private val agentClassifierService = mockk<AgentClassifierService>()
    private val agentClientService = spyk(ArcAgentClientService())

    private val conversationHandler =
        DefaultConversationHandler(
            agentRegistryService,
            agentRoutingService,
            agentClassifierService,
            agentClientService,
            lmosRuntimeConfig,
            lmosRuntimeTenantAwareCache,
        )

    @Test
    fun `should return agent response when explicitly specified`() =
        runBlocking {
            val conversationId = "conversation-id-success"
            val tenantId = "en"
            val turnId = "turn-id-success-1"

            val conversation = createConversation("UserManagementAgent")

            val agentAddress = Address(protocol = "http", uri = "localhost:8080/user-agent")

            val mockGraphQlAgentClient = mockk<GraphQlAgentClient>()
            coEvery { agentClientService.createGraphQlAgentClient(agentAddress) } returns mockGraphQlAgentClient
            coEvery { mockGraphQlAgentClient.close() } just runs
            coEvery { mockGraphQlAgentClient.callAgent(any<AgentRequest>()) } returns
                flow {
                    emit(AgentResult(messages = listOf(Message(role = "assistant", content = "Dummy response from Agent"))))
                }

            val assistantMessage =
                conversationHandler.handleConversation(conversation, conversationId, tenantId, turnId, null).first()

            assertEquals("Dummy response from Agent", assistantMessage.content)
            coVerify(exactly = 1) { mockGraphQlAgentClient.callAgent(any(), any(), any()) }
        }

    @Test
    fun `should pass subset parameter to agent registry service`() =
        runBlocking {
            val conversationId = "conversation-id-with-subset"
            val tenantId = "en"
            val turnId = "turn-id-with-subset"
            val subset = "test-subset"

            val conversation = createConversation("UserManagementAgent")

            val agentAddress = Address(protocol = "http", uri = "localhost:8080/user-agent")

            val mockGraphQlAgentClient = mockk<GraphQlAgentClient>()
            coEvery { agentClientService.createGraphQlAgentClient(agentAddress) } returns mockGraphQlAgentClient
            coEvery { mockGraphQlAgentClient.close() } just runs
            coEvery { mockGraphQlAgentClient.callAgent(any<AgentRequest>()) } returns
                flow {
                    emit(AgentResult(messages = listOf(Message(role = "assistant", content = "Response with subset"))))
                }

            // Use a spy on the agentRegistryService to verify the subset parameter is passed
            val spyAgentRegistryService = spyk(agentRegistryService)

            // Create a new conversation handler with the spy
            val handlerWithSpy =
                DefaultConversationHandler(
                    spyAgentRegistryService,
                    agentRoutingService,
                    agentClassifierService,
                    agentClientService,
                    lmosRuntimeConfig,
                    lmosRuntimeTenantAwareCache,
                )

            val assistantMessage =
                handlerWithSpy.handleConversation(conversation, conversationId, tenantId, turnId, subset).first()

            assertEquals("Response with subset", assistantMessage.content)

            // Verify that the subset parameter was passed to the agent registry service
            coVerify(exactly = 1) {
                spyAgentRegistryService.getRoutingInformation(tenantId, conversation.systemContext.channelId, subset)
            }
        }

    @Test
    fun `should throw NoRoutingInfoFoundException when no agent found in agent registry`() =
        runBlocking {
            val conversationId = "conversation-id-404"
            val tenantId = "de"
            val turnId = "404-agent-registry"

            val conversation = createConversation("UserManagementAgent")

            val mockGraphQlAgentClient = mockk<GraphQlAgentClient>()

            assertThrows<NoRoutingInfoFoundException> {
                conversationHandler
                    .handleConversation(
                        conversation,
                        conversationId,
                        tenantId,
                        turnId,
                        null,
                    ).first()
            }
            coVerify(exactly = 0) { mockGraphQlAgentClient.callAgent(any(), any(), any()) }
        }

    @Test
    fun `should throw AgentNotFoundException when matching agent not found`() =
        runBlocking {
            val conversationId = "conversation-id"
            val tenantId = "en"
            val turnId = "turn-id"

            val conversation = createConversation("UnconfiguredAgent")

            val mockGraphQlAgentClient = mockk<GraphQlAgentClient>()

            assertThrows<AgentNotFoundException> {
                conversationHandler
                    .handleConversation(
                        conversation,
                        conversationId,
                        tenantId,
                        turnId,
                        null,
                    ).first()
            }
            coVerify(exactly = 0) { mockGraphQlAgentClient.callAgent(any(), any(), any()) }
        }

    @Test
    fun `should throw AgentClientException when agent returns error`() =
        runBlocking {
            val conversationId = "conversation-id"
            val tenantId = "en"
            val turnId = "turn-id"

            val conversation = createConversation("UserManagementAgent")

            val mockGraphQlAgentClient = mockk<GraphQlAgentClient>()
            coEvery { agentClientService.createGraphQlAgentClient(any()) } returns mockGraphQlAgentClient
            coEvery { mockGraphQlAgentClient.callAgent(any(), any(), any()) } throws RuntimeException("Something went wrong")

            assertThrows<AgentClientException> {
                conversationHandler
                    .handleConversation(
                        conversation,
                        conversationId,
                        tenantId,
                        turnId,
                        null,
                    ).first()
            }
            coVerify(exactly = 1) { mockGraphQlAgentClient.callAgent(any(), any(), any()) }
        }

    private fun createConversation(agent: String) =
        Conversation(
            systemContext = SystemContext(channelId = "web"),
            userContext = UserContext(userId = "user-id", userToken = "user-token"),
            inputContext =
                InputContext(
                    messages = listOf(Message(role = "user", content = "Hello")),
                    explicitAgent = agent,
                ),
        )
}
