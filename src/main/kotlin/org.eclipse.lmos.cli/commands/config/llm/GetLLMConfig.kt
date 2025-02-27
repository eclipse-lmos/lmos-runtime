package org.eclipse.lmos.cli.commands.config.llm

import org.eclipse.lmos.cli.commands.agent.promptUser
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "get",
    description = ["Get a credential"],
)
class GetLLMConfig : Callable<Int> {

    @CommandLine.Option(names = ["-i", "--id"], description = ["Id of Credential"])
    var id: String? = null

    override fun call(): Int {
        val id = id ?: promptUser("Enter id")
        val llmConfig = DefaultLLMConfigManager().getLLMConfig(id)
        if (llmConfig == null) {
            println("LLM with ID $id not found")
            return 1
        } else {
            println(
                """
                |ID: ${llmConfig.id}
                |Model Name: ${llmConfig.modelName}
                |Base URL: ${llmConfig.baseUrl}
                |API Key: *****${llmConfig.apiKey.substring(llmConfig.apiKey.length - 4)}
                |Provider: ${llmConfig.provider}
            """.trimIndent()
            )
            return 0
        }
    }
} 
