package org.eclipse.lmos.cli.commands.agent

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "agent",
    mixinStandardHelpOptions = true,
    description = ["Agent Management Commands"],
    subcommands = [
        Create::class,
        Chat::class,
    ],
)
class Agent : Callable<Int> {
    override fun call(): Int {
        CommandLine.usage(this, System.out)
        return 0
    }
} 
