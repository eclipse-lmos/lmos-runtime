package org.kinetiqx.prism.agent.commands

import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "list", description = ["List agents or tools"], mixinStandardHelpOptions = true)
class ListCommand : Runnable {
    @CommandLine.Parameters(index = "0", description = ["What to list: 'agents' or 'tools'"])
    private lateinit var itemType: String

    override fun run() {
        when (itemType.lowercase(Locale.getDefault())) {
            "agents" -> listAgents()
            "tools" -> listTools()
            else -> System.err.println("Unknown item to list: $itemType")
        }
    }

    private fun listAgents() {
        println("Listing agents:")

        // Implement the logic to list agents here
        println("- AgentA")
        println("- AgentB")
    }

    private fun listTools() {
        println("Listing tools:")

        // Implement the logic to list tools here
        println("- ToolA")
        println("- ToolB")
    }
}
