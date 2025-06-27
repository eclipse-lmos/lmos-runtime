/*
 * // SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.model.registry

import kotlinx.serialization.Serializable
import org.eclipse.lmos.runtime.core.model.Address
import org.eclipse.lmos.runtime.core.model.Agent
import org.eclipse.lmos.runtime.core.model.AgentBuilder
import org.eclipse.lmos.runtime.core.model.AgentCapability

@Serializable
data class ChannelRouting(
    val apiVersion: String? = null, // Made nullable
    val kind: String? = null, // Made nullable
    val metadata: Metadata,
    val spec: Spec,
    val subset: String? = null,
)

@Serializable
data class Metadata(
    val name: String,
    val namespace: String,
    val labels: Labels,
    val creationTimestamp: String? = null,
    val generation: Int? = null,
    val resourceVersion: String? = null,
    val uid: String? = null,
)

@Serializable
data class Labels(
    val channel: String,
    val subset: String? = null,
    val tenant: String,
    val version: String,
)

@Serializable
data class Spec(
    val capabilityGroups: List<CapabilityGroup>,
)

@Serializable
data class CapabilityGroup(
    val name: String,
    val description: String,
    val capabilities: List<Capability>,
)

@Serializable
data class Capability(
    val name: String,
    val providedVersion: String,
    val description: String,
    val host: String,
    val requiredVersion: String? = null,
)

data class RoutingInformation(
    val agentList: List<Agent>,
    val subset: String?,
)

fun ChannelRouting.toAgent(): List<Agent> {
    val agentVersion = this.metadata.labels.version
    return this.spec.capabilityGroups
        .map { agent ->
            AgentBuilder()
                .name(agent.name)
                .description(agent.description)
                .version(agentVersion)
                .apply {
                    agent.capabilities.firstOrNull()?.let { addAddress(Address(uri = it.host)) }
                    agent.capabilities.forEach { capability ->
                        addCapability(
                            AgentCapability(
                                name = capability.name,
                                version = capability.providedVersion,
                                description = capability.description,
                            ),
                        )
                    }
                }.build()
        }.toList()
}
