package org.eclipse.lmos.cli.llm

import kotlinx.serialization.Serializable

@Serializable
data class LLMConfig(val id: String, val modelName: String, val baseUrl: String, val apiKey: String, val provider: String)