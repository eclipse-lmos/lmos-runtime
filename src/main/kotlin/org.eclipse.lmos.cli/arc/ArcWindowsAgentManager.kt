package org.eclipse.lmos.cli.arc


import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.commands.agent.AgentStatus
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.llm.LLMConfig
import org.slf4j.LoggerFactory


class ArcWindowsAgentManager : ArcAgentManager() {

    private val log = LoggerFactory.getLogger(ArcWindowsAgentManager::class.java)


    override fun startAgent(llmConfigs: List<LLMConfig>): AgentStatus {

        val agents = AGENT_PROJECTS_DIRECTORY.resolve(AgentType.ARC.name)
        if (getAgentHealthStatus() == AgentStatus.READY) {
            return AgentStatus.READY
        }

        val envVars = getEnvVars(llmConfigs)
        val command = listOf("cmd", "/c", "gradlew.bat", "-q", "--console=plain", "bootrun")

//        val command = listOf( "gradlew.bat", "-q", "--console=plain", "clean", "bootrun" )

        println("Start command: ${command.joinToString(" ")}")
        println("agents: ${agents.toFile()}")
//        executeCommand(command, envVars, agents.toFile(), false)
        val pid = executeCommandAndGetPID(agents, envVars, command)
        val checkProcessStatus = checkProcessStatus(pid)
        return if (checkProcessStatus) {
            getAgentStatus(pid)
        } else {
            AgentStatus.FAILED
        }
    }


}