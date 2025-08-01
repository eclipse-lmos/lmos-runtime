/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.config

import org.eclipse.lmos.classifier.llm.starter.ModelAgentClassifierAutoConfiguration
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.outbound.LmosOperatorAgentRegistry
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [LmosRuntimeAutoConfiguration::class, ModelAgentClassifierAutoConfiguration::class])
@TestPropertySource(
    properties = [
        "lmos.runtime.agent-registry.type=API",
        "lmos.runtime.agent-registry.base-url=http://dummy-api.com",
        "lmos.runtime.cache.ttl=600",
        "lmos.runtime.router.type=EXPLICIT", // Added router type
        "lmos.router.classifier.llm.enabled=true",
        "lmos.router.llm.provider=openai",
        "lmos.router.llm.model=dummy-model",
    ],
)
class ApiAgentRegistryLoadedConditionTest {
    @Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun `should load LmosOperatorAgentRegistry when type is API`() {
        val agentRegistryService = context.getBean(AgentRegistryService::class.java)
        assertNotNull(agentRegistryService)
        assertInstanceOf(LmosOperatorAgentRegistry::class.java, agentRegistryService)
    }
}
