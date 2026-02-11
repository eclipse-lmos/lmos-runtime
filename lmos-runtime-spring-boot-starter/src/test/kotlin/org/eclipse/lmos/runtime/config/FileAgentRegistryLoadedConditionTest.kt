/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.config

import org.eclipse.lmos.classifier.llm.starter.ModelAgentClassifierAutoConfiguration
import org.eclipse.lmos.runtime.core.channelrouting.ChannelRoutingRepository
import org.eclipse.lmos.runtime.outbound.FileBasedChannelRoutingRepository
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [RuntimeAutoConfiguration::class, ModelAgentClassifierAutoConfiguration::class, CacheAutoConfiguration::class])
@TestPropertySource(
    properties = [
        "lmos.runtime.channelRoutingRepository.type=FILE",
        "lmos.runtime.channelRoutingRepository.filename=integration-test-agent-registry.yaml",
        "lmos.runtime.router.type=EXPLICIT", // Added router type
        "lmos.runtime.disambiguation.enabled=false",
        "lmos.runtime.disambiguation.llm.provider=openai",
        "lmos.runtime.disambiguation.llm.model=dummy-model",
        "lmos.router.classifier.llm.enabled=true",
        "lmos.router.llm.provider=openai",
        "lmos.router.llm.model=dummy-model",
        "spring.cache.type=simple",
    ],
)
class FileAgentRegistryLoadedConditionTest {
    @Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun `should load FileBasedAgentRegistryService when type is FILE`() {
        val agentRegistryService = context.getBean(ChannelRoutingRepository::class.java)
        assertNotNull(agentRegistryService)
        assertInstanceOf(FileBasedChannelRoutingRepository::class.java, agentRegistryService)
    }
}
