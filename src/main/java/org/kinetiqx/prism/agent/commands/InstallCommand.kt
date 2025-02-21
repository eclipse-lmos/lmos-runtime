package org.kinetiqx.prism.agent.commands

import install
import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "install", description = ["Install an agent or tool"], mixinStandardHelpOptions = true)
class InstallCommand : Runnable {
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
        // Implement the logic to install an agent here
        install("https://github.com/Kinetiqx/Prism/releases/download/0.0.1/firstAgent.tar.gz")
    }

    private fun installTool(toolName: String?) {
        println("Installing tool: $toolName")
        // Implement the logic to install a tool here
    }
}
