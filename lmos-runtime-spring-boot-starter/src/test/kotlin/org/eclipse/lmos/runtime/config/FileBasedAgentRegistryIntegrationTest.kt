/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.config

import kotlinx.coroutines.test.runTest
import org.eclipse.lmos.classifier.llm.starter.ModelAgentClassifierAutoConfiguration
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.model.registry.toRoutingInformation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(
    classes = [
        RuntimeAutoConfiguration::class,
        ModelAgentClassifierAutoConfiguration::class,
        CacheAutoConfiguration::class,
    ],
) // Minimal context
@TestPropertySource(
    properties = [
        "lmos.runtime.agent-registry.type=FILE",
        "lmos.runtime.agent-registry.filename=integration-test-agent-registry.yaml",
        "lmos.runtime.router.type=EXPLICIT",
        "lmos.runtime.cache.ttl=600",
        "lmos.runtime.disambiguation.enabled=false",
        "lmos.runtime.disambiguation.llm.provider=openai",
        "lmos.runtime.disambiguation.llm.model=dummy-model",
        "lmos.router.classifier.llm.enabled=true",
        "lmos.router.llm.provider=openai",
        "lmos.router.llm.model=dummy-model",
        "spring.cache.type=simple",
    ],
)
class FileBasedAgentRegistryIntegrationTest {
    @Autowired
    private lateinit var channelRoutingRepository: ChannelRoutingRepository

    // If ConversationHandler is too complex to set up, test AgentRegistryService directly
    // @Autowired
    // private lateinit var conversationHandler: ConversationHandler

    @Test
    fun `should retrieve routing information using FileBasedAgentRegistryService via Spring context`() =
        runTest {
            val routingInfo = channelRoutingRepository.getChannelRouting("integ-acme", "web", "stable").toRoutingInformation()
            assertNotNull(routingInfo)
            assertEquals(1, routingInfo.agentList.size)
            assertEquals("integ-contract-agent", routingInfo.agentList[0].name)
            assertEquals("stable", routingInfo.subset)

            // Test a case without subset
            val routingInfoNoSubset = channelRoutingRepository.getChannelRouting("integ-acme", "app", null).toRoutingInformation()
            assertNotNull(routingInfoNoSubset)
            assertEquals(1, routingInfoNoSubset.agentList.size)
            assertEquals("integ-app-agent", routingInfoNoSubset.agentList[0].name)
            assertNotNull(routingInfoNoSubset.subset)
        }

    @Test
    fun `should throw NoRoutingInfoFoundException for non-existent entry via Spring context`() =
        runTest {
            assertThrows(NoRoutingInfoFoundException::class.java) {
                channelRoutingRepository.getChannelRouting("non-existent", "channel", null).toRoutingInformation()
            }
        }
}
