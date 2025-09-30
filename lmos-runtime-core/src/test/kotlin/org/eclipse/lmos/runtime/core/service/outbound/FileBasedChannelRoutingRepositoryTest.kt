/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.service.outbound

import kotlinx.coroutines.test.runTest
import org.eclipse.lmos.runtime.core.ChannelRoutingRepositoryType
import org.eclipse.lmos.runtime.core.RuntimeConfiguration
import org.eclipse.lmos.runtime.core.channelrouting.toRoutingInformation
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.outbound.FileBasedChannelRoutingRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.net.URL

class FileBasedChannelRoutingRepositoryTest {
    private fun getTestResourcePath(fileName: String): String =
        FileBasedChannelRoutingRepositoryTest::class.java.classLoader
            .getResource(fileName)
            ?.path
            ?: throw IllegalStateException("Test resource $fileName not found.")

    @Test
    fun `should load and parse valid YAML and find routing information`() =
        runTest {
            val lmosRuntimeConfig =
                getLmosRuntimeConfig("test-agent-registry.yaml")
            val service = FileBasedChannelRoutingRepository(lmosRuntimeConfig)
            val routingInfo = service.getChannelRouting("acme", "web", "stable").toRoutingInformation()

            assertNotNull(routingInfo)
            assertEquals(1, routingInfo.agentList.size)
            assertEquals("contract-agent", routingInfo.agentList[0].name)
            assertEquals("stable", routingInfo.subset)
        }

    private fun getLmosRuntimeConfig(fileName: String): RuntimeConfiguration {
        val lmosRuntimeConfig =
            RuntimeConfiguration(
                channelRoutingRepository =
                    RuntimeConfiguration.ChannelRoutingRepositoryConfig(
                        type = ChannelRoutingRepositoryType.FILE,
                        fileName = fileName,
                    ),
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
        return lmosRuntimeConfig
    }

    @Test
    fun `should find routing information when no subset is requested and a stable match exists`() =
        runTest {
            // This test assumes that if a specific subset is not found or not requested,
            // a match with no subset label is considered.
            // The current implementation requires subset to be null in labels for this.
            val service = FileBasedChannelRoutingRepository(getLmosRuntimeConfig("test-agent-registry.yaml"))
            val routingInfo = service.getChannelRouting("acme", "web", null).toRoutingInformation() // Requesting without subset

            assertNotNull(routingInfo)
            assertEquals(1, routingInfo.agentList.size)
            assertEquals("contract-agent", routingInfo.agentList[0].name)
            // The routingInfo.subset should reflect the subset from the matched label, which is null in this case.
            assertNotNull(routingInfo.subset)
        }

    @Test
    fun `should find routing information for different tenant and channel`() =
        runTest {
            val service = FileBasedChannelRoutingRepository(getLmosRuntimeConfig("test-agent-registry.yaml"))
            val routingInfo = service.getChannelRouting("another-tenant", "app", "beta").toRoutingInformation()

            assertNotNull(routingInfo)
            assertEquals(1, routingInfo.agentList.size)
            assertEquals("beta-feature-agent", routingInfo.agentList[0].name)
            assertEquals("beta", routingInfo.subset)
        }

    @Test
    fun `should throw NoRoutingInfoFoundException when no match found`() =
        runTest {
            val service = FileBasedChannelRoutingRepository(getLmosRuntimeConfig("test-agent-registry.yaml"))

            assertThrows(NoRoutingInfoFoundException::class.java) {
                service.getChannelRouting("nonexistent", "channel", null).toRoutingInformation()
            }
        }

    @Test
    fun `should throw NoRoutingInfoFoundException when subset does not match`() =
        runTest {
            val service = FileBasedChannelRoutingRepository(getLmosRuntimeConfig("test-agent-registry.yaml"))

            assertThrows(NoRoutingInfoFoundException::class.java) {
                service.getChannelRouting("acme", "web", "nonexistent-subset").toRoutingInformation()
            }
        }

    @Test
    fun `should throw IllegalArgumentException for non-existent file`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                FileBasedChannelRoutingRepository(getLmosRuntimeConfig("non-existent-file.yaml"))
            }
        assertTrue(exception.message?.contains("Agent registry file not found") == true)
    }

    @Test
    fun `should throw IllegalArgumentException for malformed YAML`() =
        runTest {
            val malformedContent = "channelRoutings: - metadata: name: broken"
            val fileName = "malformed.yaml"

            // Create a custom classloader with the temporary content
            withTemporaryResource(fileName, malformedContent) {
                val exception =
                    assertThrows(IllegalArgumentException::class.java) {
                        FileBasedChannelRoutingRepository(getLmosRuntimeConfig(fileName))
                    }
                assertTrue(exception.message?.contains("Error parsing agent registry file") == true)
            }
        }

    @Test
    fun `should handle empty channelRoutings list`() =
        runTest {
            val emptyContent = "channelRoutings: []"
            val fileName = "empty.yaml"

            withTemporaryResource(fileName, emptyContent) {
                val service = FileBasedChannelRoutingRepository(getLmosRuntimeConfig(fileName))
                assertThrows(NoRoutingInfoFoundException::class.java) {
                    service.getChannelRouting("acme", "web", "stable").toRoutingInformation()
                }
            }
        }

    private fun <T> withTemporaryResource(
        fileName: String,
        content: String,
        block: () -> T,
    ): T {
        val tempClassLoader = InMemoryClassLoader(mapOf(fileName to content.toByteArray()))
        val originalClassLoader = Thread.currentThread().contextClassLoader

        return try {
            Thread.currentThread().contextClassLoader = tempClassLoader
            block()
        } finally {
            Thread.currentThread().contextClassLoader = originalClassLoader
        }
    }

    @Test
    fun `should handle completely empty YAML file`() =
        runTest {
            val fileName = "completely-empty.yaml"
            val emptyContent = "" // Empty content

            // Use withTemporaryResource to create temporary classpath resource
            withTemporaryResource(fileName, emptyContent) {
                // Kaml might throw an exception if it can't deserialize to AgentRegistryDocument (e.g. "channelRoutings" is missing)
                val exception =
                    assertThrows(IllegalArgumentException::class.java) {
                        FileBasedChannelRoutingRepository(getLmosRuntimeConfig(fileName))
                    }
                assertTrue(exception.message?.contains("Error parsing agent registry file") == true) {
                    exception.message
                }
            }
        }

    class InMemoryClassLoader(
        private val resources: Map<String, ByteArray>,
        parent: ClassLoader = Thread.currentThread().contextClassLoader,
    ) : ClassLoader(parent) {
        override fun getResourceAsStream(name: String): InputStream? = resources[name]?.inputStream() ?: super.getResourceAsStream(name)

        override fun getResource(name: String): URL? =
            if (resources.containsKey(name)) {
                // Create a data URL for the resource
                URL(
                    "data:text/plain;base64," +
                        java.util.Base64
                            .getEncoder()
                            .encodeToString(resources[name]),
                )
            } else {
                super.getResource(name)
            }
    }
}
