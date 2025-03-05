package org.eclipse.lmos.cli.arc


import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.commands.agent.AgentStatus
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.llm.LLMConfig
import org.slf4j.LoggerFactory
import java.nio.file.Path


class ArcMacOSAgentManager : ArcAgentManager() {

    private val log = LoggerFactory.getLogger(ArcMacOSAgentManager::class.java)


    override fun startAgent(llmConfigs: List<LLMConfig>): AgentStatus {

        val agents = AGENT_PROJECTS_DIRECTORY.resolve(AgentType.ARC.name)
        if (getAgentHealthStatus() == AgentStatus.READY) {
            return AgentStatus.READY
        }

        val envVars = getEnvVars(llmConfigs)
        val commandArray = createStartCommand(agents)
        val pid = executeCommandAndGetPID(agents, envVars, commandArray)
        val checkProcessStatus = checkProcessStatus(pid)
        return if (checkProcessStatus) {
            getAgentStatus(pid)
        } else {
            AgentStatus.FAILED
        }
    }

    private fun createStartCommand(agents: Path?): List<String> {
        return listOf( "./gradlew", "-q", "--console=plain", "clean", "bootrun" )
    }

}