package org.eclipse.lmos.cli.commands.agent

import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "install", description = ["Install an agent or tool"], mixinStandardHelpOptions = true)
class Install : Runnable {
    @CommandLine.Parameters(index = "0", description = ["Item type to install: 'agent' or 'tool'"])
    private lateinit var itemType: String

    @CommandLine.Parameters(index = "1", description = ["Name of the item to install"])
    private lateinit var itemName: String

    override fun run() {
        when (itemType.lowercase(Locale.getDefault())) {
            "agent" -> installAgent(itemName)
            "tool" -> installTool(itemName)
            else -> System.err.println("Unknown item type to install: $itemType")
        }
    }

    private fun installAgent(agentName: String?) {
        println("Installing agent: $agentName")
        TODO("Implement the logic to install an agent here")
    }

    private fun installTool(toolName: String?) {
        println("Installing tool: $toolName")
        TODO("Implement the logic to install a tool here")
    }
}
