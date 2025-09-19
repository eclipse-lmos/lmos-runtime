/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.inbound

import kotlinx.coroutines.flow.Flow
import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.runtime.config.RuntimeAutoConfiguration
import org.eclipse.lmos.runtime.core.inbound.ConversationHandler
import org.eclipse.lmos.runtime.core.inbound.DefaultConversationHandler
import org.eclipse.lmos.runtime.core.model.Address
import org.eclipse.lmos.runtime.core.model.Agent
import org.eclipse.lmos.runtime.core.model.AssistantMessage
import org.eclipse.lmos.runtime.core.model.Conversation
import org.eclipse.lmos.runtime.core.model.registry.RoutingInformation
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRegistryService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRoutingService
import org.eclipse.lmos.runtime.outbound.ArcAgentClientService
import org.eclipse.lmos.runtime.outbound.DefaultAgentClassifierService
import org.eclipse.lmos.runtime.outbound.LmosAgentRoutingService
import org.eclipse.lmos.runtime.properties.RuntimeProperties
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [RuntimeAutoConfiguration::class, CacheAutoConfiguration::class],
)
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "lmos.runtime.router.type=LLM", "lmos.runtime.openAi=dummyOpenAiKey", "lmos.runtime.disambiguation.enabled=false",
        "lmos.runtime.disambiguation.llm.provider=openai", "lmos.runtime.disambiguation.llm.model=dummy-model",
        "lmos.router.classifier.llm.enabled=true", "spring.cache.type=simple",
    ],
)
@Import(LmosRuntimeAutoConfigurationCustomBeansTest.CustomBeanConfig::class)
class LmosRuntimeAutoConfigurationCustomBeansTest {
    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var runtimeProperties: RuntimeProperties

    @Test
    fun `should not load ArcAgentClientService as AgentClientService`() {
        val agentClientService = applicationContext.getBean(AgentClientService::class.java)
        assertFalse(agentClientService is ArcAgentClientService)
    }

    @Test
    fun `should not load LmosAgentRoutingService as AgentRoutingService`() {
        val agentRoutingService = applicationContext.getBean(AgentRoutingService::class.java)
        assertFalse(agentRoutingService is LmosAgentRoutingService)
    }

    @Test
    fun `should not load LmosAgentClassifierService as AgentClassifierService`() {
        val defaultAgentClassifierService = applicationContext.getBean(AgentClassifierService::class.java)
        assertFalse(defaultAgentClassifierService is DefaultAgentClassifierService)
    }

    @Test
    fun `should not load DefaultConversationHandler as ConversationService`() {
        val conversationService = applicationContext.getBean(ConversationHandler::class.java)
        assertFalse(conversationService is DefaultConversationHandler)
    }

    @TestConfiguration
    open class CustomBeanConfig {
        @Bean
        open fun agentClientService(): AgentClientService =
            object : AgentClientService {
                override suspend fun askAgent(
                    conversation: Conversation,
                    conversationId: String,
                    turnId: String,
                    agentName: String,
                    agentAddress: Address,
                    subset: String?,
                ): Flow<AssistantMessage> {
                    TODO("Not yet implemented")
                }
            }

        @Bean
        open fun agentClassifierService(): AgentClassifierService =
            object : AgentClassifierService {
                override suspend fun classify(
                    conversation: Conversation,
                    agents: List<Agent>,
                    tenant: String,
                    subset: String?,
                ): ClassificationResult {
                    TODO("Not yet implemented")
                }
            }

        @Bean
        open fun agentRoutingService(): AgentRoutingService =
            object : AgentRoutingService {
                override suspend fun resolveAgentForConversation(
                    conversation: Conversation,
                    agentList: List<Agent>,
                ): Agent {
                    TODO("Not yet implemented")
                }
            }

        @Bean
        open fun agentRegistryService(): AgentRegistryService =
            object : AgentRegistryService {
                override suspend fun getRoutingInformation(
                    tenantId: String,
                    channelId: String,
                    subset: String?,
                ): RoutingInformation {
                    TODO("Not yet implemented")
                }
            }

        @Bean
        open fun conversationService(): ConversationHandler =
            object : ConversationHandler {
                override suspend fun handleConversation(
                    conversation: Conversation,
                    conversationId: String,
                    tenantId: String,
                    turnId: String,
                    subset: String?,
                ): Flow<AssistantMessage> {
                    TODO("Not yet implemented")
                }
            }
    }
}
