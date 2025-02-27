package org.eclipse.lmos.cli.commands.agent

import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "list", description = ["List agents or tools"], mixinStandardHelpOptions = true)
class List : Runnable {
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
        TODO("Implement the logic to list agents here")
    }

    private fun listTools() {
        TODO("Implement the logic to list tools here")
    }
}
