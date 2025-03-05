package org.eclipse.lmos.cli.commands.config.llm

import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.registry.agent.AgentRegistry
import org.eclipse.lmos.cli.utils.CliPrinter
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "list",
    description = ["List all credentials"],
)
class ListLLMConfig : Callable<Int> {

    private val log = LoggerFactory.getLogger(AgentRegistry::class.java)

    override fun call(): Int {

        val listLLMConfig = DefaultLLMConfigManager().listLLMConfig()
        if (listLLMConfig.isEmpty()) {
            CliPrinter.printError("Configuration for LLM Not found")
        } else {
            CliPrinter.printSuccess("Found ${listLLMConfig.size} LLM with the following IDs:")
            listLLMConfig
                .forEach {
                    println("""
                |   ID: $it
            """.trimMargin())
                }
        }
        return 0
    }
}


