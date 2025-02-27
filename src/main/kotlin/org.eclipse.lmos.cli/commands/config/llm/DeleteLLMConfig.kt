package org.eclipse.lmos.cli.commands.config.llm

import org.eclipse.lmos.cli.commands.agent.promptUser
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "delete",
    description = ["Delete a credential or all credentials"],
)
class DeleteLLMConfig : Callable<Int> {

    @CommandLine.Option(names = ["-i", "--id"], description = ["Id of Credential"])
    var id: String? = null

    override fun call(): Int {
        val id = id ?: promptUser("Enter id")
        val deleteLLMConfig = DefaultLLMConfigManager().deleteLLMConfig(id)
        if (deleteLLMConfig == null) {
            println("No LLM found with id: $id")
        } else {
            println("Deleted LLM with id: $id, modelName: ${deleteLLMConfig.modelName}")
        }
        return 0
    }
} 
