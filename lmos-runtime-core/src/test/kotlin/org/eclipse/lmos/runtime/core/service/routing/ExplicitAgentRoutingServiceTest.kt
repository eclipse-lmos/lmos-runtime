/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.lmos.runtime.core.service.routing

import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.runtime.core.exception.AgentNotFoundException
import org.eclipse.lmos.runtime.core.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExplicitAgentRoutingServiceTest {
    @Test
    fun resolves_agent_when_explicit_agent_name_matches() =
        runBlocking {
            val conversation =
                Conversation(
                    inputContext =
                        InputContext(
                            messages = listOf(),
                            explicitAgent = "AgentX",
                        ),
                    systemContext = SystemContext(channelId = "channel1"),
                    userContext = UserContext(userId = "user1", userToken = "token1"),
                )

            val service = ExplicitAgentRoutingService()
            val resolvedAgent = service.resolveAgentForConversation(conversation, getAgents())

            assertEquals("AgentX", resolvedAgent.name)
        }

    @Test
    fun throws_exception_when_agent_not_matches() {
        val conversation =
            Conversation(
                inputContext =
                    InputContext(
                        messages = listOf(),
                        explicitAgent = "Agent1",
                    ),
                systemContext =
                    SystemContext(
                        channelId = "channel1",
                    ),
                userContext =
                    UserContext(
                        userId = "user1",
                        userToken = null,
                    ),
            )
        val service = ExplicitAgentRoutingService()
        assertFailsWith<AgentNotFoundException> {
            runBlocking {
                service.resolveAgentForConversation(conversation, getAgents())
            }
        }
    }

    @Test
    fun test_resolve_agent_throws_exception_when_explicit_agent_not_provided() {
        val conversation =
            Conversation(
                inputContext =
                    InputContext(
                        messages = listOf(),
                        explicitAgent = null,
                    ),
                systemContext =
                    SystemContext(
                        channelId = "channel1",
                    ),
                userContext =
                    UserContext(
                        userId = "user1",
                        userToken = null,
                    ),
            )
        val service = ExplicitAgentRoutingService()
        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                service.resolveAgentForConversation(conversation, getAgents())
            }
        }
    }

    private fun getAgents() =
        listOf(
            Agent(
                name = "AgentX",
                version = "1.0",
                description = "Test Agent",
                capabilities = listOf(),
                addresses = listOf(),
            ),
            Agent(
                name = "AgentY",
                version = "1.0",
                description = "Another Agent",
                capabilities = listOf(),
                addresses = listOf(),
            ),
        )
}
