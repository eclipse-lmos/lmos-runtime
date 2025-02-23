package org.kinetiqx.prism.agent.commands

import org.kinetiqx.prism.credentials.commands.ListCommand
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "agent",
    mixinStandardHelpOptions = true,
    description = ["Agent Management Commands"],
    subcommands = [
        ChatCommand::class,
        InstallCommand::class,
        ListCommand::class,
    ],
)
class AgentCommand : Callable<Int> {
    override fun call(): Int {
        CommandLine.usage(this, System.out)
        return 0
    }
} 
