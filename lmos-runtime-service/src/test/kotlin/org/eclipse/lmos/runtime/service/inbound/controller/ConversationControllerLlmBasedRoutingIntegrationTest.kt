/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.lmos.runtime.service.inbound.controller

import io.mockk.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.arc.agent.client.graphql.GraphQlAgentClient
import org.eclipse.lmos.arc.api.AgentRequest
import org.eclipse.lmos.arc.api.AgentResult
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.runtime.core.model.*
import org.eclipse.lmos.runtime.outbound.ArcAgentClientService
import org.eclipse.lmos.runtime.service.constants.LmosServiceConstants.Endpoints.BASE_PATH
import org.eclipse.lmos.runtime.service.constants.LmosServiceConstants.Endpoints.CHAT_URL
import org.eclipse.lmos.runtime.service.constants.LmosServiceConstants.Headers.TURN_ID
import org.eclipse.lmos.runtime.test.BaseWireMockTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.*

@SpringBootTest(properties = [
    "lmos.runtime.router.type=llm",
    "lmos.runtime.open-ai.provider=other",
    "lmos.runtime.open-ai.key=some-api-key",
    "lmos.runtime.open-ai.url=http://localhost:8080/llm",
    "lmos.runtime.open-ai.model=some-model",
    "lmos.runtime.open-ai.max-tokens=2000",
    "lmos.runtime.open-ai.temperature=2.0",
    "lmos.runtime.open-ai.format=json_object"])
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(AbstractConversationControllerIntegrationTest.TestConfig::class)
class ConversationControllerLlmBasedRoutingIntegrationTest : AbstractConversationControllerIntegrationTest()