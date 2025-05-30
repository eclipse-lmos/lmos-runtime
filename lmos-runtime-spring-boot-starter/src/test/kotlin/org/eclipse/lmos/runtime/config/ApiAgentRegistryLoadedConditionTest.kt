package org.eclipse.lmos.runtime.config

import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.outbound.LmosOperatorAgentRegistry
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [LmosRuntimeAutoConfiguration::class])
@TestPropertySource(properties = [
    "lmos.runtime.agent-registry.type=API",
    "lmos.runtime.agent-registry.base-url=http://dummy-api.com",
    "lmos.runtime.cache.ttl=600",
    "lmos.runtime.router.type=EXPLICIT" // Added router type
])
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
