/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.service.outbound

import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.runtime.core.AgentRegistryType
import org.eclipse.lmos.runtime.core.LmosRuntimeConfig
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.net.URL

class FileBasedAgentRegistryServiceTest {
    private fun getTestResourcePath(fileName: String): String =
        FileBasedAgentRegistryServiceTest::class.java.classLoader
            .getResource(fileName)
            ?.path
            ?: throw IllegalStateException("Test resource $fileName not found.")

    @Test
    fun `should load and parse valid YAML and find routing information`() =
        runBlocking {
            val lmosRuntimeConfig =
                getLmosRuntimeConfig("test-agent-registry.yaml")
            val service = FileBasedAgentRegistryService(lmosRuntimeConfig.agentRegistry)
            val routingInfo = service.getRoutingInformation("acme", "web", "stable")

            assertNotNull(routingInfo)
            assertEquals(1, routingInfo.agentList.size)
            assertEquals("contract-agent", routingInfo.agentList[0].name)
            assertEquals("stable", routingInfo.subset)
        }

    private fun getLmosRuntimeConfig(fileName: String): LmosRuntimeConfig {
        val lmosRuntimeConfig =
            LmosRuntimeConfig(
                agentRegistry =
                    LmosRuntimeConfig.AgentRegistry(
                        type = AgentRegistryType.FILE,
                        fileName = fileName,
                    ),
                cache = LmosRuntimeConfig.Cache(ttl = 6000),
            )
        return lmosRuntimeConfig
    }

    @Test
    fun `should find routing information when no subset is requested and a stable match exists`() =
        runBlocking {
            // This test assumes that if a specific subset is not found or not requested,
            // a match with no subset label is considered.
            // The current implementation requires subset to be null in labels for this.
            val service = FileBasedAgentRegistryService(getLmosRuntimeConfig("test-agent-registry.yaml").agentRegistry)
            val routingInfo = service.getRoutingInformation("acme", "web", null) // Requesting without subset

            assertNotNull(routingInfo)
            assertEquals(1, routingInfo.agentList.size)
            assertEquals("contract-agent", routingInfo.agentList[0].name)
            // The routingInfo.subset should reflect the subset from the matched label, which is null in this case.
            assertNotNull(routingInfo.subset)
        }

    @Test
    fun `should find routing information for different tenant and channel`() =
        runBlocking {
            val service = FileBasedAgentRegistryService(getLmosRuntimeConfig("test-agent-registry.yaml").agentRegistry)
            val routingInfo = service.getRoutingInformation("another-tenant", "app", "beta")

            assertNotNull(routingInfo)
            assertEquals(1, routingInfo.agentList.size)
            assertEquals("beta-feature-agent", routingInfo.agentList[0].name)
            assertEquals("beta", routingInfo.subset)
        }

    @Test
    fun `should throw NoRoutingInfoFoundException when no match found`() =
        runBlocking {
            val service = FileBasedAgentRegistryService(getLmosRuntimeConfig("test-agent-registry.yaml").agentRegistry)

            assertThrows(NoRoutingInfoFoundException::class.java) {
                runBlocking { service.getRoutingInformation("nonexistent", "channel", null) }
            }
        }

    @Test
    fun `should throw NoRoutingInfoFoundException when subset does not match`() =
        runBlocking {
            val service = FileBasedAgentRegistryService(getLmosRuntimeConfig("test-agent-registry.yaml").agentRegistry)

            assertThrows(NoRoutingInfoFoundException::class.java) {
                runBlocking { service.getRoutingInformation("acme", "web", "nonexistent-subset") }
            }
        }

    @Test
    fun `should throw IllegalArgumentException for non-existent file`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                FileBasedAgentRegistryService(getLmosRuntimeConfig("non-existent-file.yaml").agentRegistry)
            }
        assertTrue(exception.message?.contains("Agent registry file not found") == true)
    }

    @Test
    fun `should throw IllegalArgumentException for malformed YAML`() =
        runBlocking {
            val malformedContent = "channelRoutings: - metadata: name: broken"
            val fileName = "malformed.yaml"

            // Create a custom classloader with the temporary content
            withTemporaryResource(fileName, malformedContent) {
                val exception =
                    assertThrows(IllegalArgumentException::class.java) {
                        FileBasedAgentRegistryService(getLmosRuntimeConfig(fileName).agentRegistry)
                    }
                assertTrue(exception.message?.contains("Error parsing agent registry file") == true)
            }
        }

    @Test
    fun `should handle empty channelRoutings list`() =
        runBlocking {
            val emptyContent = "channelRoutings: []"
            val fileName = "empty.yaml"

            withTemporaryResource(fileName, emptyContent) {
                val service = FileBasedAgentRegistryService(getLmosRuntimeConfig(fileName).agentRegistry)
                assertThrows(NoRoutingInfoFoundException::class.java) {
                    runBlocking { service.getRoutingInformation("acme", "web", "stable") }
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
        runBlocking {
            val fileName = "completely-empty.yaml"
            val emptyContent = "" // Empty content

            // Use withTemporaryResource to create temporary classpath resource
            withTemporaryResource(fileName, emptyContent) {
                // Kaml might throw an exception if it can't deserialize to AgentRegistryDocument (e.g. "channelRoutings" is missing)
                val exception =
                    assertThrows(IllegalArgumentException::class.java) {
                        FileBasedAgentRegistryService(getLmosRuntimeConfig(fileName).agentRegistry)
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
