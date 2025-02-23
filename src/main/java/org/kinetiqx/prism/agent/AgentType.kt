package org.kinetiqx.prism.agent

enum class AgentType {
    ARC;

    companion object: Iterable<String> {
        fun fromOrNull(value: String?): AgentType? = entries.find { it.name.equals(value, ignoreCase = true) }

        override fun iterator(): Iterator<String> = entries.map { it.name }.iterator()
    }
}
