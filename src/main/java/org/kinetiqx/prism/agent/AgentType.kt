package org.kinetiqx.prism.agent

enum class AgentType {
    ARC,
    DUMMY;

    companion object {
        fun fromOrNull(value: String?): AgentType? = entries.find { it.name.equals(value, ignoreCase = true) }
    }
}
