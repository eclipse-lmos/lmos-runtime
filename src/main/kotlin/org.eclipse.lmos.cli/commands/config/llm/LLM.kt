package org.eclipse.lmos.cli.commands.config.llm

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "llm",
    mixinStandardHelpOptions = true,
    description = ["LLM Configuration Management Commands"],
    subcommands = [
        AddLLMConfig::class,
        GetLLMConfig::class,
        ListLLMConfig::class,
        UpdateLLMConfig::class,
        DeleteLLMConfig::class,
    ],
)
class LLM : Callable<Int> {
    override fun call(): Int {
        CommandLine.usage(this, System.out)
        return 0
    }
} 
