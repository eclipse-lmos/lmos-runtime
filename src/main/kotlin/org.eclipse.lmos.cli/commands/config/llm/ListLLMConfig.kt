package org.eclipse.lmos.cli.commands.config.llm

import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "list",
    description = ["List all credentials"],
)
class ListLLMConfig : Callable<Int> {
    override fun call(): Int {

        printlnHeader("Listing all LLMs")

        val listLLMConfig = DefaultLLMConfigManager().listLLMConfig()
        if (listLLMConfig.isEmpty()) {
            println("No LLM found")
        } else {
            println("Found ${listLLMConfig.size} LLM with the following IDs:")
            listLLMConfig
                .forEach {
                    println("""
                ID: $it
            """.trimIndent())
                }
        }
        return 0
    }
}


fun printlnHeader(s: String) {
    CommandLine.Help.Ansi.AUTO.string(
        "@|bold,green Using id: $s |@"
    ).also(::println)
}
