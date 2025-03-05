package org.eclipse.lmos.cli.starter

import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.commands.agent.AgentStatus
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.credential.manager.executeCommand
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.llm.LLMConfig

class WindowsAgentStarter : AgentStarter {
    override fun startAgent(): String {

        val agents = AGENT_PROJECTS_DIRECTORY.resolve(AgentType.ARC.name)

        //        val deleteCommand = "cd $projectDirectory && if [ -f application.log ]; then rm application.log && echo \"deleted\"; fi"
        val deleteCommand = arrayOf("cmd.exe", "/c", "cd /d $agents && if exist application.log (del application.log && echo deleted)")
        executeCommand(deleteCommand)

//        val startCommand = "cd $projectDirectory && nohup ./gradlew -q --console=plain bootrun --args='$params'>application.log 2>&1 < /dev/null &"

//        actual fun chatCommand(directory: String, agentName: String): String = "cd $projectDirectory && ./gradlew -q --console=plain arc -Pagent=$agentName"

        val params = "--management.endpoint.shutdown.enabled=true --management.endpoints.web.exposure.include=\"*\""

        val defaultLLMConfigManager = DefaultLLMConfigManager()
        val llmConfigs: List<LLMConfig> = defaultLLMConfigManager.listLLMConfig().mapNotNull {
            defaultLLMConfigManager.getLLMConfig(it)
        }.toList()


        val envVars = llmConfigs.mapIndexed { index, config ->
            """
        set ARC_AI_CLIENTS_${index}_ID=${config.id}
        set ARC_AI_CLIENTS_${index}_CLIENT=${config.provider}
        set ARC_AI_CLIENTS_${index}_URL=${config.baseUrl}
        set ARC_AI_CLIENTS_${index}_APIKEY=${config.apiKey}
        set ARC_AI_CLIENTS_${index}_MODELNAME=${config.modelName}
        """.trimIndent()
        }.joinToString("\n")

        val startCommand = arrayOf(
            "cmd.exe", "/c", """
    cd /d $agents &&
    ${envVars.replace("\n", " && ")} &&
    start /b /min "" gradlew -q --console=plain bootrun --args="$params" > application.log 2>&1
    """.trimIndent()
        )

        run {val result = executeCommand(startCommand, false)
            println("Command: ${startCommand.contentToString()}, Result: $result")
        }

        return runAtFixedRate(setOf("STARTED", "FAILED"), 2000, 30, 4000) { checkForStartUpStatus(agents) }


    }

    override fun checkStatus(): AgentStatus {
        TODO("Not yet implemented")
    }
}