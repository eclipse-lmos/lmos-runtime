package org.eclipse.lmos.runtime.core.service.outbound

import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class FileBasedAgentRegistryServiceTest {

    private fun getTestResourcePath(fileName: String): String {
        return FileBasedAgentRegistryServiceTest::class.java.classLoader.getResource(fileName)?.path
            ?: throw IllegalStateException("Test resource \$fileName not found.")
    }

    @Test
    fun `should load and parse valid YAML and find routing information`() = runBlocking {
        val service = FileBasedAgentRegistryService(getTestResourcePath("test-agent-registry.yaml"))
        val routingInfo = service.getRoutingInformation("acme", "web", "stable")

        assertNotNull(routingInfo)
        assertEquals(1, routingInfo.agentList.size)
        assertEquals("contract-agent", routingInfo.agentList[0].name)
        assertEquals("stable", routingInfo.subset)
    }

    @Test
    fun `should find routing information when no subset is requested and a generic match exists`() = runBlocking {
        // This test assumes that if a specific subset is not found or not requested,
        // a match with no subset label is considered.
        // The current implementation requires subset to be null in labels for this.
        val service = FileBasedAgentRegistryService(getTestResourcePath("test-agent-registry.yaml"))
        val routingInfo = service.getRoutingInformation("acme", "web", null) // Requesting without subset

        assertNotNull(routingInfo)
        assertEquals(1, routingInfo.agentList.size)
        assertEquals("contract-agent-generic", routingInfo.agentList[0].name)
        // The routingInfo.subset should reflect the subset from the matched label, which is null in this case.
        assertNull(routingInfo.subset)
    }

    @Test
    fun `should find routing information for different tenant and channel`() = runBlocking {
        val service = FileBasedAgentRegistryService(getTestResourcePath("test-agent-registry.yaml"))
        val routingInfo = service.getRoutingInformation("another-tenant", "app", "beta")

        assertNotNull(routingInfo)
        assertEquals(1, routingInfo.agentList.size)
        assertEquals("beta-feature-agent", routingInfo.agentList[0].name)
        assertEquals("beta", routingInfo.subset)
    }

    @Test
    fun `should throw NoRoutingInfoFoundException when no match found`() = runBlocking {
        val service = FileBasedAgentRegistryService(getTestResourcePath("test-agent-registry.yaml"))

        assertThrows(NoRoutingInfoFoundException::class.java) {
            runBlocking { service.getRoutingInformation("nonexistent", "channel", null) }
        }
    }

    @Test
    fun `should throw NoRoutingInfoFoundException when subset does not match`() = runBlocking {
        val service = FileBasedAgentRegistryService(getTestResourcePath("test-agent-registry.yaml"))

        assertThrows(NoRoutingInfoFoundException::class.java) {
            runBlocking { service.getRoutingInformation("acme", "web", "nonexistent-subset") }
        }
    }

    @Test
    fun `should throw IllegalArgumentException for non-existent file`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            FileBasedAgentRegistryService("non-existent-file.yaml")
        }
        assertTrue(exception.message?.contains("Agent registry file not found") == true)
    }

    @Test
    fun `should throw IllegalArgumentException for malformed YAML`(@TempDir tempDir: Path) = runBlocking {
        val malformedFile = tempDir.resolve("malformed.yaml").toFile()
        malformedFile.writeText("channelRoutings: - metadata: name: broken") // Invalid YAML structure

        val exception = assertThrows(IllegalArgumentException::class.java) {
            FileBasedAgentRegistryService(malformedFile.absolutePath)
        }
        assertTrue(exception.message?.contains("Error parsing agent registry file") == true)
    }

    @Test
    fun `should handle empty channelRoutings list`(@TempDir tempDir: Path) = runBlocking {
        val emptyRegistryFile = tempDir.resolve("empty.yaml").toFile()
        emptyRegistryFile.writeText("channelRoutings: []")

        val service = FileBasedAgentRegistryService(emptyRegistryFile.absolutePath)
        assertThrows(NoRoutingInfoFoundException::class.java) {
            runBlocking { service.getRoutingInformation("acme", "web", "stable") }
        }
    }

    @Test
    fun `should handle completely empty YAML file`(@TempDir tempDir: Path) = runBlocking {
        val emptyFile = tempDir.resolve("completely-empty.yaml").toFile()
        emptyFile.writeText("") // Empty content

        // Kaml might throw an exception if it can't deserialize to AgentRegistryDocument (e.g. "channelRoutings" is missing)
        val exception = assertThrows(IllegalArgumentException::class.java) {
             FileBasedAgentRegistryService(emptyFile.absolutePath)
        }
        assertTrue(exception.message?.contains("Error parsing agent registry file") == true)
    }
}
