package org.eclipse.lmos.cli.commands.config.llm

import org.eclipse.lmos.cli.commands.agent.promptUser
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.llm.LLMConfig
import org.eclipse.lmos.cli.utils.CliPrinter
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "update",
    description = ["Update a credential"],
)
class UpdateLLMConfig : Callable<Int> {

    @CommandLine.Option(names = ["-i", "--id"], description = ["Id of WindowsCredential"])
    var id: String? = null

    override fun call(): Int {

        val id = id ?: promptUser("Enter id")
        val llmConfigManager = DefaultLLMConfigManager()
        val llmConfig = llmConfigManager.getLLMConfig(id)
        if (llmConfig == null) {
            CliPrinter.printError("LLM not found: $id")
        } else {
            val modelName = promptUser("Enter model name", true) ?: llmConfig.modelName
            val baseUrl = promptUser("Enter base-url", true) ?: llmConfig.baseUrl
            val apiKey = promptUser("Enter api-key", true) ?: llmConfig.apiKey
            val provider = promptUser("Enter provider", true) ?: llmConfig.provider
            val updatedLLM = LLMConfig(id, modelName, baseUrl, apiKey, provider)
            llmConfigManager.updateLLMConfig(updatedLLM).let {
                CliPrinter.printSuccess("""
                LLM Config added successfully
                |   ID: ${llmConfig.id}
                |   Model Name: ${llmConfig.modelName}
                |   Base URL: ${llmConfig.baseUrl}
                |   Provider: ${llmConfig.provider}
                |   Key: ********
            """.trimMargin())
            }
        }
        return 0
    }
} 
