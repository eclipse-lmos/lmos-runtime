package org.eclipse.lmos.cli.commands.config.llm

import org.eclipse.lmos.cli.commands.agent.promptUser
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.utils.CliPrinter
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "get",
    description = ["Get a credential"],
)
class GetLLMConfig : Callable<Int> {

    @CommandLine.Option(names = ["-i", "--id"], description = ["Id of WindowsCredential"], required = false)
    var id: String? = null

    override fun call(): Int {
        val id = id ?: promptUser("Enter id")
        val llmConfig = DefaultLLMConfigManager().getLLMConfig(id)
        if (llmConfig == null) {
            CliPrinter.printError("LLM Config with ID $id not found")
            return 0
        } else {
            CliPrinter.printSuccess(
                """
                |   ID: ${llmConfig.id}
                |   Model Name: ${llmConfig.modelName}
                |   Base URL: ${llmConfig.baseUrl}
                |   Provider: ${llmConfig.provider}
                |   API Key: ********
            """.trimMargin()
            )
            return 0
        }
    }
} 
