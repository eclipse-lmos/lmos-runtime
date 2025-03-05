package org.eclipse.lmos.cli.arc


import jakarta.ws.rs.core.Response
import org.eclipse.lmos.cli.agent.AgentManager
import org.eclipse.lmos.cli.commands.agent.AgentInfo
import org.eclipse.lmos.cli.commands.agent.AgentStatus
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.llm.LLMConfig
import org.eclipse.lmos.cli.outbound.GenericRestClient
import org.eclipse.lmos.cli.utils.runAtFixedRate
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path


abstract class ArcAgentManager : AgentManager {

    private val log = LoggerFactory.getLogger(ArcAgentManager::class.java)

    fun getEnvVars(llmConfigs: List<LLMConfig>): Map<String, String> =
        llmConfigs.flatMapIndexed { index, config ->
            listOf(
                "ARC_AI_CLIENTS_${index}_ID" to config.id,
                "ARC_AI_CLIENTS_${index}_CLIENT" to config.provider,
                "ARC_AI_CLIENTS_${index}_URL" to config.baseUrl,
                "ARC_AI_CLIENTS_${index}_APIKEY" to config.apiKey,
                "ARC_AI_CLIENTS_${index}_MODELNAME" to config.modelName
            )
        }.toMap()


        override fun getAgentStatus(pid: Long): AgentStatus {
        val result = runAtFixedRate(
            pollingDurationMillis = 2000L,
            initialDelayMillis = 3000L,
            fn = { getAgentHealthStatus() },
            breakFn = { tempResult -> isAgentReady(tempResult as AgentStatus) },
            loopFn = { checkProcessStatus(pid) }
        )

        return if (result == AgentStatus.READY) {
            AgentStatus.READY
        } else {
            AgentStatus.FAILED
        }
    }

    protected open fun getAgentHealthStatus(): AgentStatus {
        val restClient = GenericRestClient()
        val response: Response
        try {
            response = restClient.create("http://localhost:9090/health").get()
        } catch (e: Exception) {
            log.error("Failed to connect to agent app", e)
            return AgentStatus.ERROR
        }
        if (response.statusInfo.family == Response.Status.Family.SUCCESSFUL) {
            return AgentStatus.READY
        }
        return AgentStatus.STARTING
    }

    fun executeCommandAndGetPID(
        agents: Path?,
        envVars: Map<String, String>,
        command: List<String>
    ): Long {

        val processBuilder = ProcessBuilder(command)

        if (agents?.toFile()?.isDirectory == true) {
            processBuilder.directory(agents.toFile())
        }

        if (envVars.isNotEmpty()) {
            envVars.forEach { processBuilder.environment()[it.key] = it.value }
        }
        val logFile = agents?.resolve("application.log")?.toFile() ?: File("application.log")
        processBuilder.redirectOutput(logFile)
        processBuilder.redirectError(logFile)
        val process = processBuilder.start()
        return process.pid()
    }

    fun checkProcessStatus(pid: Long): Boolean {
        val handle = ProcessHandle.of(pid).orElseThrow {
            log.error("Process with PID $pid not found.")
            NoSuchElementException("Agent Process not found.")
        }
        val isAlive = handle.isAlive
        return isAlive
    }

    protected val isAgentReady: (AgentStatus) -> Boolean = { it == AgentStatus.READY }

    override fun getLogs(agentInfo: AgentInfo): List<String> {
        return AGENT_PROJECTS_DIRECTORY.resolve(agentInfo.type.name)
            .resolve("application.log")
            .toFile()
            .readLines()
            .toCollection(mutableListOf())
    }

    override fun shutdownAgent(agentInfo: AgentInfo) {
        val restClient = GenericRestClient()
        try {
            restClient.create("http://localhost:9090/shutdown").post("")
        } catch (e: Exception) {
            log.error("Failed to connect to agent app", e)
        }
    }


}