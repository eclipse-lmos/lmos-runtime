/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.inbound.controller

import io.mockk.*
import org.eclipse.lmos.runtime.core.model.*
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(AbstractConversationControllerIntegrationTest.TestConfig::class)
class ConversationControllerExplicitAgentRoutingIntegrationTest : AbstractConversationControllerIntegrationTest()
