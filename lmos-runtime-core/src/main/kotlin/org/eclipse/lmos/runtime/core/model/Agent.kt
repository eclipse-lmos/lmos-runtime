/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.model

data class Agent(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val capabilities: List<AgentCapability>,
    val addresses: List<Address>,
)

data class Address(
    val protocol: String = "http",
    val uri: String,
)

data class AgentCapability(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val subset: String? = "",
)

class AgentBuilder {
    private var id: String = ""
    private var name: String = ""
    private var version: String = ""
    private var description: String = ""
    private var capabilities: MutableList<AgentCapability> = mutableListOf()
    private var addresses: MutableList<Address> = mutableListOf()

    fun id(id: String) = apply { this.id = id }

    fun name(name: String) = apply { this.name = name }

    fun version(version: String) = apply { this.version = version }

    fun description(description: String) = apply { this.description = description }

    fun addCapability(capability: AgentCapability) = apply { this.capabilities.add(capability) }

    fun capabilities(capabilities: List<AgentCapability>) = apply { this.capabilities.addAll(capabilities) }

    fun addAddress(address: Address) = apply { this.addresses.add(address) }

    fun addresses(addresses: List<Address>) = apply { this.addresses.addAll(addresses) }

    fun build(): Agent =
        Agent(
            id = id,
            name = name,
            version = version,
            description = description,
            capabilities = capabilities.toList(),
            addresses = addresses,
        )
}
