package org.eclipse.lmos.cli.discovery.agent

import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.commands.agent.AgentDiscovery
import org.eclipse.lmos.cli.commands.agent.AgentInfo

class AgentDiscoveryFactory {
    companion object {
        fun createAgentDiscovery(agentType: AgentType): AgentDiscovery {
            return when (agentType) {
                AgentType.ARC -> ArcAgentDiscovery()
            }
        }
    }

}

class ArcAgentDiscovery : AgentDiscovery {

    override fun discoverAgents(): List<AgentInfo> {
        //todo - discover agents from configured agent repos
//        val folders = Files.list(AGENT_PROJECTS_DIRECTORY).filter { it.isDirectory() }.toList()
//        if(folders.isEmpty() || folders.size > 1) {
//            println("No or multiple agent projects found: $folders")
//            throw IllegalStateException("No or multiple agent projects found")
//        } else {
//
//        }
        return emptyList()
    }

}