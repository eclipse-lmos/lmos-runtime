package org.eclipse.lmos.runtime.core.model.registry

import kotlinx.serialization.Serializable // Or Jackson if that's standard

@Serializable // Or Jackson annotations
data class AgentRegistryDocument(
    val channelRoutings: List<ChannelRouting>
)
