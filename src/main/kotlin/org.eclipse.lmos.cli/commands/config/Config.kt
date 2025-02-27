package org.eclipse.lmos.cli.commands.config

import org.eclipse.lmos.cli.commands.config.llm.LLM
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "config",
    mixinStandardHelpOptions = true,
    description = ["Configuration Management Commands"],
    subcommands = [
        LLM::class
    ],
)
class Config : Callable<Int> {
    override fun call(): Int {
        CommandLine.usage(this, System.out)
        return 0
    }
} 
