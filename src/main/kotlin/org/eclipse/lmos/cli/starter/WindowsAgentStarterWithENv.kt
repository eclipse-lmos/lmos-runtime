package org.eclipse.lmos.cli.starter

import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.commands.agent.AgentStatus
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.credential.manager.executeCommand
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.llm.LLMConfig

class WindowsAgentStarterWithENv : AgentStarter {
    override fun startAgent(): String {

        val agents = AGENT_PROJECTS_DIRECTORY.resolve(AgentType.ARC.name)

        // Delete the application.log if it exists
        val deleteCommand = arrayOf("cmd.exe", "/c", "cd /d $agents && if exist application.log (del application.log && echo deleted)")
        executeCommand(deleteCommand)

        val params = "--management.endpoint.shutdown.enabled=true --management.endpoints.web.exposure.include=\"*\""

        val defaultLLMConfigManager = DefaultLLMConfigManager()
        val llmConfigs: List<LLMConfig> = defaultLLMConfigManager.listLLMConfig().mapNotNull {
            defaultLLMConfigManager.getLLMConfig(it)
        }.toList()

        // Prepare environment variables
        val envVarsMap = llmConfigs.mapIndexed { index, config ->
            mapOf(
                "ARC_AI_CLIENTS_${index}_ID" to config.id,
                "ARC_AI_CLIENTS_${index}_CLIENT" to config.provider,
                "ARC_AI_CLIENTS_${index}_URL" to config.baseUrl,
                "ARC_AI_CLIENTS_${index}_APIKEY" to config.apiKey,
                "ARC_AI_CLIENTS_${index}_MODELNAME" to config.modelName
            )
        }.fold(mutableMapOf<String, String>()) { acc, map -> acc.apply { putAll(map) } }

        // Build the command to execute
        val command = arrayOf(
            "cmd.exe", "/c", "gradlew.bat", "-q", "--console=plain", "bootrun", "--args=$params", " > application.log 2>&1"
        )

        //    start /b /min "" gradlew -q --console=plain bootrun --args="$params" > application.log 2>&1

//        output = process.inputStream.bufferedReader().use(BufferedReader::readText)


        val processBuilder = ProcessBuilder(*command)
            .directory(agents.toFile())
//            .redirectErrorStream(true)
//            .redirectOutput(ProcessBuilder.Redirect.to(agents.resolve("application.log").toFile()))

        // Set the environment variables
        val env = processBuilder.environment()
        env.putAll(envVarsMap)

        // Start the process
        val process = processBuilder.start()

        // Optionally, wait for the process to start if needed
        // process.waitFor()

        return runAtFixedRate(setOf("STARTED", "FAILED"), 2000, 30, 4000) { checkForStartUpStatus(agents) }
    }

    override fun checkStatus(): AgentStatus {
        TODO("Not yet implemented")
    }
}