package org.eclipse.lmos.cli.commands.config.llm

import org.eclipse.lmos.cli.commands.agent.promptUser
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.utils.CliPrinter
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "delete",
    description = ["Delete a credential"],
)
class DeleteLLMConfig : Callable<Int> {

    @CommandLine.Option(names = ["-i", "--id"], description = ["Id of WindowsCredential"])
    var id: String? = null

    override fun call(): Int {

        val id = id ?: promptUser("Enter id")
        val llmConfigManager = DefaultLLMConfigManager()
        val existingLLMConfig = llmConfigManager.getLLMConfig(id)

        if (existingLLMConfig == null) {
            CliPrinter.printError("LLM Config not found with id: $id")
            return 0
        } else {
            llmConfigManager.deleteLLMConfig(id)
            CliPrinter.printSuccess("Deleted LLM with id: $id, modelName: ${existingLLMConfig.modelName}")
            return 0
        }
    }
} 
