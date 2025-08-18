/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.inbound

import org.eclipse.lmos.runtime.core.cache.LmosRuntimeTenantAwareCache
import org.eclipse.lmos.runtime.core.cache.TenantAwareInMemoryCache
import org.eclipse.lmos.runtime.core.disambiguation.DefaultDisambiguationHandler
import org.eclipse.lmos.runtime.core.disambiguation.DisambiguationHandler
import org.eclipse.lmos.runtime.core.inbound.ConversationHandler
import org.eclipse.lmos.runtime.core.inbound.DefaultConversationHandler
import org.eclipse.lmos.runtime.core.service.outbound.AgentClassifierService
import org.eclipse.lmos.runtime.core.service.outbound.AgentClientService
import org.eclipse.lmos.runtime.core.service.outbound.AgentRoutingService
import org.eclipse.lmos.runtime.core.service.routing.ExplicitAgentRoutingService
import org.eclipse.lmos.runtime.outbound.ArcAgentClientService
import org.eclipse.lmos.runtime.outbound.LmosAgentClassifierService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class LmosRuntimeAutoConfigurationTest {
    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Test
    fun `should load ArcAgentClientService as AgentClientService`() {
        val agentClientService = applicationContext.getBean(AgentClientService::class.java)
        assertTrue(agentClientService is ArcAgentClientService)
    }

    @Test
    fun `should load ExplicitAgentRoutingService as AgentRoutingService`() {
        val agentRoutingService = applicationContext.getBean(AgentRoutingService::class.java)
        assertTrue(agentRoutingService is ExplicitAgentRoutingService)
    }

    @Test
    fun `should not load LmosAgentClassifierService as AgentClassifierService`() {
        val agentClassifierService = applicationContext.getBean(AgentClassifierService::class.java)
        assertTrue(agentClassifierService is LmosAgentClassifierService)
    }

    @Test
    fun `should load DefaultDisambiguationHandler as DisambiguationHandler`() {
        val disambiguationHandler = applicationContext.getBean(DisambiguationHandler::class.java)
        assertTrue(disambiguationHandler is DefaultDisambiguationHandler)
    }

    @Test
    fun `should load DefaultConversationHandler as ConversationHandler`() {
        val conversationHandler = applicationContext.getBean(ConversationHandler::class.java)
        assertTrue(conversationHandler is DefaultConversationHandler)
    }

    @Test
    fun `should load TenantAwareInMemoryCache as LmosRuntimeTenantAwareCache`() {
        val cache = applicationContext.getBean(LmosRuntimeTenantAwareCache::class.java)
        assertTrue(cache is TenantAwareInMemoryCache)
    }
}
