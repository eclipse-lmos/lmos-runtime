package org.eclipse.lmos.cli.commands.config.llm

import org.eclipse.lmos.cli.commands.agent.promptUser
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.llm.LLMConfig
import org.eclipse.lmos.cli.utils.CliPrinter.printError
import org.eclipse.lmos.cli.utils.CliPrinter.printSuccess
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "add", description = ["Add a new credential"])
class AddLLMConfig : Callable<Int> {

    @CommandLine.Option(names = ["-i", "--id"], description = ["Id of WindowsCredential"])
    var id: String? = null

    override fun call(): Int {

        val id = id ?: promptUser("Enter id")

        DefaultLLMConfigManager().getLLMConfig(id)?.let {
            printError("LLM Config already exists with id: $id")
            return 0
        }
        val modelName = promptUser("Enter model name")
        val baseUrl = promptUser("Enter base-url")
        val apiKey = promptUser("Enter api-key")
        val provider = promptUser("Enter provider")
        val llmConfig = LLMConfig(id, modelName, baseUrl, apiKey, provider)
        DefaultLLMConfigManager().addLLMConfig(llmConfig).let {
            printSuccess("""
            |LLM Config added successfully:
            |   ID: ${llmConfig.id}
            |   Model Name: ${llmConfig.modelName}
            |   Base URL: ${llmConfig.baseUrl}
            |   Provider: ${llmConfig.provider}
            |   Key: ********
            """.trimMargin())
        }
        return 0
    }
}
