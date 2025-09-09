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

@SpringBootTest(
    properties = [
        "lmos.runtime.router.type=llm",
        "lmos.runtime.open-ai.provider=other",
        "lmos.runtime.open-ai.key=some-api-key",
        "lmos.runtime.open-ai.url=http://localhost:8080/llm",
        "lmos.runtime.open-ai.model=some-model",
        "lmos.runtime.open-ai.max-tokens=2000",
        "lmos.runtime.open-ai.temperature=2.0",
        "lmos.runtime.open-ai.format=json_object",
    ],
)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(AbstractConversationControllerIntegrationTest.TestConfig::class)
class ConversationControllerLlmBasedRoutingIntegrationTest : AbstractConversationControllerIntegrationTest()
