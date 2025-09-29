/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.outbound

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.RuntimeConfiguration.ChannelRoutingRepositoryConfig
import org.eclipse.lmos.runtime.core.exception.NoChannelFoundException
import org.eclipse.lmos.runtime.test.BaseWireMockTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Test for OperatorChannelRepository using Wiremock stub for channel endpoint.
 */
class OperatorChannelRepositoryTest : BaseWireMockTest() {
    private lateinit var repository: OperatorChannelRepository

    @BeforeEach
    fun setup() {
        val runtimeConfig =
            RuntimeConfiguration(
                ChannelRoutingRepositoryConfig(baseUrl = wireMockServer.baseUrl()),
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
        repository = OperatorChannelRepository(runtimeConfig)
    }

    @Test
    fun `getChannel returns Channel on success`() =
        runTest {
            val tenantId = "acme"
            val channelId = "ivr"
            val result = repository.getChannel(tenantId, channelId)
            assertNotNull(result)
        }

    @Test
    fun `getChannels returns all channels for tenant`() =
        runTest {
            val tenantId = "acme"
            val result = repository.getChannels(tenantId)
            assertNotNull(result)
            assert(result.isNotEmpty())
            assert(result.first().metadata.name == "acme-ivr-stable")
        }

    @Test
    fun `getChannel throws exception if channel not found`() =
        runTest {
            val tenantId = "acme"
            val channelId = "notfound"
            assertThrows(NoChannelFoundException::class.java) {
                runBlocking {
                    repository.getChannel(tenantId, channelId)
                }
            }
        }
}
