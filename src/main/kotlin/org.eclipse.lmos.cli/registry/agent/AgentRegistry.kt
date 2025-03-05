package org.eclipse.lmos.cli.registry.agent

import kotlinx.serialization.builtins.ListSerializer
import net.mamoe.yamlkt.Yaml
import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.commands.agent.AgentInfo
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENTS_REGISTRY
import org.slf4j.LoggerFactory

class AgentRegistry {

    private val log = LoggerFactory.getLogger(AgentRegistry::class.java)


    fun registerAgent(agentInfo: AgentInfo): Boolean {
        val agentRegistryFile = getAgentRegistry(agentInfo.name)
        var agentInfoList: MutableList<AgentInfo> = mutableListOf()
        if (!agentRegistryFile.exists()) {
            val createNewFile = agentRegistryFile.createNewFile()
            log.info("agentRegistryFile file created: $createNewFile")
            agentRegistryFile.setWritable(true, true)
        } else {
            agentInfoList = try {
                Yaml.decodeFromString(ListSerializer(AgentInfo.serializer()), agentRegistryFile.readText()).toMutableList()
            } catch (e: Exception) {
                log.error("Error reading agent registry file", e)
                return false
            }
        }
        agentInfoList.add(agentInfo)
        agentRegistryFile.writeText(Yaml.encodeToString(ListSerializer(AgentInfo.serializer()), agentInfoList))
        return true
    }

    private fun getAgentRegistry(agentName: String) = AGENTS_REGISTRY.resolve("${agentName.substring(0, 1)}_agents.yaml").toFile()

    fun findAgent(agentName: String): AgentInfo? {
        val agentRegistryFile = getAgentRegistry(agentName)
        if(!agentRegistryFile.exists()) {
            return null
        }
        val agentInfoList: List<AgentInfo> =
            Yaml.decodeFromString(ListSerializer(AgentInfo.serializer()), agentRegistryFile.readText())
        return agentInfoList.firstOrNull { it.name.lowercase() == agentName.lowercase() }
    }

    fun findAgent(type: AgentType, agentName: String): AgentInfo? {
        val agentRegistryFile = getAgentRegistry(agentName)
        if(!agentRegistryFile.exists()) {
            return null
        }
        val agentInfoList: List<AgentInfo> =
            Yaml.decodeFromString(ListSerializer(AgentInfo.serializer()), agentRegistryFile.readText())
        return agentInfoList.firstOrNull { it.name.lowercase() == agentName.lowercase() && it.type == type }
    }

}