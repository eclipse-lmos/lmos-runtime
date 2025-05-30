/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.service.outbound

import com.charleskorn.kaml.Yaml
import org.eclipse.lmos.runtime.core.exception.NoRoutingInfoFoundException
import org.eclipse.lmos.runtime.core.model.registry.AgentRegistryDocument
import org.eclipse.lmos.runtime.core.model.registry.RoutingInformation
import org.eclipse.lmos.runtime.core.model.registry.toAgent
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

class FileBasedAgentRegistryService(
    private val filePath: String,
) : AgentRegistryService {
    private val log = LoggerFactory.getLogger(FileBasedAgentRegistryService::class.java)
    private val agentRegistryDocument: AgentRegistryDocument

    init {
        log.info("Initializing FileBasedAgentRegistryService with file: \$filePath")
        try {
            val yamlContent = File(filePath).readText()
            // If using kaml (kotlinx-serialization-yaml)
            agentRegistryDocument = Yaml.default.decodeFromString(AgentRegistryDocument.serializer(), yamlContent)
            // If using Jackson:
            // val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
            // agentRegistryDocument = mapper.readValue(yamlContent, AgentRegistryDocument::class.java)
            log.info("Successfully loaded and parsed agent registry file: \$filePath")
        } catch (e: FileNotFoundException) {
            log.error("Agent registry file not found at path: \$filePath", e)
            throw IllegalArgumentException("Agent registry file not found: \$filePath", e)
        } catch (e: Exception) {
            // Catching general exception for parsing errors
            log.error("Error parsing agent registry file: \$filePath", e)
            throw IllegalArgumentException("Error parsing agent registry file: \$filePath. Details: \${e.message}", e)
        }
    }

    override suspend fun getRoutingInformation(
        tenantId: String,
        channelId: String,
        subset: String?,
    ): RoutingInformation {
        val effectiveSubset = subset ?: "any"
        log.debug(
            "Searching for routing information for tenant: $tenantId, channel: $channelId, subset: $effectiveSubset",
        )

        val matchingChannelRouting =
            agentRegistryDocument.channelRoutings.find { cr ->
                val labels = cr.metadata.labels
                val tenantMatch = labels.tenant == tenantId
                val channelMatch = labels.channel == channelId
                // If requested subset is null, match only if label's subset is also null.
                // If requested subset is non-null, match only if label's subset is equal.
                val subsetMatch = labels.subset == subset
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
