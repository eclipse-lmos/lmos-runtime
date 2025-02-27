package org.eclipse.lmos.cli.commands.config.llm

import org.eclipse.lmos.cli.commands.agent.promptUser
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.llm.LLMConfig
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "add", description = ["Add a new credential"])
class AddLLMConfig : Callable<Int> {

    @CommandLine.Option(names = ["-i", "--id"], description = ["Id of Credential"])
    var id: String? = null

    override fun call(): Int {

        val id = id ?: promptUser("Enter id")
        val modelName = promptUser("Enter model name")
        val baseUrl = promptUser("Enter base-url")
        val apiKey = promptUser("Enter api-key")
        val provider = promptUser("Enter provider")
        val llmConfig = LLMConfig(id, modelName, baseUrl, apiKey, provider)
        val maskedApiKey = if (llmConfig.apiKey.length >= 4) {
            "*****${llmConfig.apiKey.takeLast(4)}"
        } else {
            "*****"
        }
        DefaultLLMConfigManager().addLLMConfig(llmConfig)?.let {
            println("""LLM Config added successfully
                |ID: ${llmConfig.id}
                |Model Name: ${llmConfig.modelName}
                |Base URL: ${llmConfig.baseUrl}
                |API Key: $maskedApiKey
                |Provider: ${llmConfig.provider}
            """.trimMargin())
        }
        return 0
    }
}
