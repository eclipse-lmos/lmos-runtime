/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.config

import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [LmosRuntimeAutoConfiguration::class]) // Minimal context
@TestPropertySource(
    properties = [
        "lmos.runtime.agent-registry.type=FILE",
        "lmos.runtime.agent-registry.filename=integration-test-agent-registry.yaml",
        "lmos.runtime.router.type=EXPLICIT",
        "lmos.runtime.cache.ttl=600",
    ],
)
class FileBasedAgentRegistryIntegrationTest {
    @Autowired
    private lateinit var agentRegistryService: AgentRegistryService

    // If ConversationHandler is too complex to set up, test AgentRegistryService directly
    // @Autowired
    // private lateinit var conversationHandler: ConversationHandler

    @Test
    fun `should retrieve routing information using FileBasedAgentRegistryService via Spring context`() =
        runBlocking {
            val routingInfo = agentRegistryService.getRoutingInformation("integ-acme", "web", "stable")
            assertNotNull(routingInfo)
            assertEquals(1, routingInfo.agentList.size)
            assertEquals("integ-contract-agent", routingInfo.agentList[0].name)
            assertEquals("stable", routingInfo.subset)

            // Test a case without subset
            val routingInfoNoSubset = agentRegistryService.getRoutingInformation("integ-acme", "app", null)
            assertNotNull(routingInfoNoSubset)
            assertEquals(1, routingInfoNoSubset.agentList.size)
            assertEquals("integ-app-agent", routingInfoNoSubset.agentList[0].name)
            assertNotNull(routingInfoNoSubset.subset)
        }

    @Test
    fun `should throw NoRoutingInfoFoundException for non-existent entry via Spring context`() =
        runBlocking {
            assertThrows(NoRoutingInfoFoundException::class.java) {
                runBlocking {
                    agentRegistryService.getRoutingInformation("non-existent", "channel", null)
                }
            }
        }
}
