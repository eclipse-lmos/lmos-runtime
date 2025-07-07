/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.config

import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.core.service.outbound.FileBasedAgentRegistryService
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [LmosRuntimeAutoConfiguration::class])
@TestPropertySource(
    properties = [
        "lmos.runtime.agent-registry.type=FILE",
        "lmos.runtime.agent-registry.filename=integration-test-agent-registry.yaml",
        "lmos.runtime.cache.ttl=600",
        "lmos.runtime.router.type=EXPLICIT", // Added router type
    ],
)
class FileAgentRegistryLoadedConditionTest {
    @Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun `should load FileBasedAgentRegistryService when type is FILE`() {
        val agentRegistryService = context.getBean(AgentRegistryService::class.java)
        assertNotNull(agentRegistryService)
        assertInstanceOf(FileBasedAgentRegistryService::class.java, agentRegistryService)
    }
}
