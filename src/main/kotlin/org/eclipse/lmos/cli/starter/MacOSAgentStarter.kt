package org.eclipse.lmos.cli.starter


import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.commands.agent.AgentStatus
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.credential.manager.executeCommand
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.llm.LLMConfig


class MacOSAgentStarter: AgentStarter {
    override fun startAgent(): String {

        val agents = AGENT_PROJECTS_DIRECTORY.resolve(AgentType.ARC.name)

        //        val deleteCommand = "cd $projectDirectory && if [ -f application.log ]; then rm application.log && echo \"deleted\"; fi"
        val deleteCommand = arrayOf("sh", "-c", "cd $agents && if [ -f application.log ]; then rm application.log && echo \"deleted\"; fi")
        val result = executeCommand(deleteCommand)
        println("Delete command: ${deleteCommand.contentToString()}, Result: $result")

//        val startCommand = "cd $projectDirectory && nohup ./gradlew -q --console=plain bootrun --args='$params'>application.log 2>&1 < /dev/null &"

//        actual fun chatCommand(directory: String, agentName: String): String = "cd $projectDirectory && ./gradlew -q --console=plain arc -Pagent=$agentName"

        val params = "--management.endpoint.shutdown.enabled=true --management.endpoints.web.exposure.include=\"*\""

        val defaultLLMConfigManager = DefaultLLMConfigManager()
        val llmConfigs: List<LLMConfig> = defaultLLMConfigManager.listLLMConfig().mapNotNull {
            defaultLLMConfigManager.getLLMConfig(it)
        }.toList()


        val envVars = llmConfigs.mapIndexed { index, config ->
            """
        export ARC_AI_CLIENTS_${index}_ID=${config.id}
        export ARC_AI_CLIENTS_${index}_CLIENT=${config.provider}
        export ARC_AI_CLIENTS_${index}_URL=${config.baseUrl}
        export ARC_AI_CLIENTS_${index}_APIKEY=${config.apiKey}
        export ARC_AI_CLIENTS_${index}_MODELNAME=${config.modelName}
        """.trimIndent()
        }.joinToString("\n")

        val startCommand = arrayOf(
            "sh", "-c", """
        cd $agents &&
        $envVars &&
        nohup ./gradlew -q --console=plain bootrun --args='$params' > application.log 2>&1 < /dev/null &
        """.trimIndent()
        )

        println("Start Command: ${startCommand.contentToString()}")
        run {
            executeCommand(startCommand, false)
            println("Completed Command: ${startCommand.contentToString()}")
        }

        return runAtFixedRate(setOf("STARTED", "FAILED"), 2000, 30, 4000) { checkForStartUpStatus(agents) }
    }

    override fun checkStatus(): AgentStatus {
        TODO("Not yet implemented")
    }
}