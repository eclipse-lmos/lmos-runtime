/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.outbound

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.RuntimeConfiguration.AgentRegistry
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.test.BaseWireMockTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OperatorChannelRoutingRepositoryTest : BaseWireMockTest() {
    private lateinit var repository: OperatorChannelRoutingRepository

    @BeforeEach
    fun setup() {
        val runtimeConfig =
            RuntimeConfiguration(
                AgentRegistry(baseUrl = wireMockServer.baseUrl()),
                cache = RuntimeConfiguration.Cache(ttl = 6000),
                disambiguation =
                    RuntimeConfiguration.Disambiguation(
                        enabled = false,
                        llm =
                            RuntimeConfiguration.ChatModel(
                                provider = "openai",
                                model = "some-model",
                            ),
                    ),
            )
        repository = OperatorChannelRoutingRepository(runtimeConfig)
    }

    @Test
    fun `getChannelRouting returns ChannelRouting on success`() =
        runTest {
            val tenantId = "en"
            val channelId = "web"

            val result = repository.getChannelRouting(tenantId, channelId, null, null)
            Assertions.assertNotNull(result)
        }

    @Test
    fun `getChannelRouting throws NoRoutingInfoFoundException on 404`() =
        runTest {
            val tenantId = "en"
            val channelId = "fail"

            assertThrows(NoRoutingInfoFoundException::class.java) {
                runBlocking { repository.getChannelRouting(tenantId, channelId, null, null) }
            }
        }
}
