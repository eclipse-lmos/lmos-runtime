package org.kinetiqx.prism.agent.commands

import org.kinetiqx.prism.agent.AgentType
import picocli.CommandLine

@CommandLine.Command(name = "create", description = ["Chat with the system"], mixinStandardHelpOptions = true)
class CreateCommand : Runnable {

    @CommandLine.Option(names = ["-t", "--type"], arity = "0..1", description = ["agent type, default ARC"])
    private var type: AgentType? = null

    override fun run() {
        type = type ?: run {
            print("Enter agent type ${AgentType.entries}: ")
            val input = readlnOrNull()?.uppercase()
            AgentType.fromOrNull(input) ?: AgentType.ARC // Use default if input is invalid
        }
        println("Agent Type: $type")
        // Implement the logic to handle the chat query here
    }

}
