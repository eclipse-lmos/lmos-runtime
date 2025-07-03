/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.service.outbound

import com.charleskorn.kaml.Yaml
import org.eclipse.lmos.runtime.core.LmosRuntimeConfig
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.model.registry.AgentRegistryDocument
import org.eclipse.lmos.runtime.core.model.registry.RoutingInformation
import org.eclipse.lmos.runtime.core.model.registry.toAgent
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException

class FileBasedAgentRegistryService(
    private val agentRegistryConfig: LmosRuntimeConfig.AgentRegistry,
) : AgentRegistryService {
    private val log = LoggerFactory.getLogger(FileBasedAgentRegistryService::class.java)
    private val agentRegistryDocument: AgentRegistryDocument

    init {
        val fileName =
            agentRegistryConfig.fileName ?: throw IllegalArgumentException(
                "LMOS runtime agent registry type is FILE, but 'lmos.runtime.agent-registry.filename' is not configured.",
            )
        log.info("Initializing FileBasedAgentRegistryService with file: $fileName")
        try {
            val yamlContent = ResourceLoader.loadResourceAsString(fileName, this::class.java)
            agentRegistryDocument = Yaml.default.decodeFromString(AgentRegistryDocument.serializer(), yamlContent)
            log.info("Successfully loaded and parsed agent registry file: $fileName")
        } catch (e: FileNotFoundException) {
            log.error("Agent registry file not found at path: $fileName", e)
            throw IllegalArgumentException("Agent registry file not found: $fileName", e)
        } catch (e: Exception) {
            // Catching general exception for parsing errors
            log.error("Error parsing agent registry file: $fileName", e)
            throw IllegalArgumentException("Error parsing agent registry file: $fileName. Details: ${e.message}", e)
        }
    }

    override suspend fun getRoutingInformation(
        tenantId: String,
        channelId: String,
        subset: String?,
    ): RoutingInformation {
        val effectiveSubset = subset ?: agentRegistryConfig.defaultSubset
        log.debug(
            "Searching for routing information for tenant: $tenantId, channel: $channelId, subset: $subset, effectiveSubset: $effectiveSubset",
        )

        val matchingChannelRouting =
            agentRegistryDocument.channelRoutings.find { cr ->
                val labels = cr.metadata.labels
                val tenantMatch = labels.tenant == tenantId
                val channelMatch = labels.channel == channelId
                // If requested subset is null, match only if label's subset is also null.
                // If requested subset is non-null, match only if label's subset is equal.
                val subsetMatch = labels.subset == effectiveSubset
                tenantMatch && channelMatch && subsetMatch
            }

        if (matchingChannelRouting == null) {
            log.warn(
                "No routing information found for tenant: $tenantId, channel: $channelId, subset: $effectiveSubset",
            )
            throw NoRoutingInfoFoundException(
                "No routing information found in file for tenant: $tenantId, channel: $channelId, subset: $effectiveSubset",
            )
        }

        log.info(
            "Found routing information for tenant: $tenantId, channel: $channelId, subset: $effectiveSubset: ${matchingChannelRouting.metadata.name}",
        )
        // Use the existing toAgent() extension function and RoutingInformation data class
        return RoutingInformation(
            agentList = matchingChannelRouting.toAgent(), // toAgent() is now on ChannelRouting
            subset = matchingChannelRouting.metadata.labels.subset, // Or from the request 'subset' if preferred
        )
    }
}

object ResourceLoader {
    private val log = LoggerFactory.getLogger(ResourceLoader::class.java)

    fun loadResourceAsString(
        fileName: String,
        clazz: Class<*> = ResourceLoader::class.java,
    ): String {
        log.debug("Attempting to load resource: $fileName")

        // Try current class classloader first
        val inputStream =
            clazz.classLoader.getResourceAsStream(fileName)
                ?: clazz.getResourceAsStream(fileName)
                ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(fileName)
                ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(fileName)
                ?: throw FileNotFoundException("Resource not found in classpath: $fileName")

        return inputStream.bufferedReader().use { it.readText() }
    }

    fun resourceExists(
        fileName: String,
        clazz: Class<*> = ResourceLoader::class.java,
    ): Boolean =
        try {
            clazz.classLoader.getResource(fileName) != null ||
                clazz.getResource(fileName) != null
        } catch (e: Exception) {
            false
        }
}
