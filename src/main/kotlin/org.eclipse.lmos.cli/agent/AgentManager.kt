package org.eclipse.lmos.cli.agent

import org.eclipse.lmos.cli.commands.agent.AgentInfo
import org.eclipse.lmos.cli.commands.agent.AgentStatus
import org.eclipse.lmos.cli.llm.LLMConfig

interface AgentManager {

    fun startAgent(llmConfigs: List<LLMConfig>): AgentStatus
    fun getAgentStatus(pid: Long): AgentStatus
    fun getLogs(agentInfo: AgentInfo): List<String>
    fun shutdownAgent(agentInfo: AgentInfo)
}

