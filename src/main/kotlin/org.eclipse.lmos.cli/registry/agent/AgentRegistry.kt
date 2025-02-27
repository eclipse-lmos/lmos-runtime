package org.eclipse.lmos.cli.registry.agent

import kotlinx.serialization.builtins.ListSerializer
import net.mamoe.yamlkt.Yaml
import org.eclipse.lmos.cli.commands.agent.AgentInfo
import org.eclipse.lmos.cli.constants.LmosCliConstants.projectDir
import kotlin.io.path.readText
import kotlin.io.path.writeText

class AgentRegistry {

    fun findAgent(agentName: String): AgentInfo {
        val agentRegistryFile = projectDir.resolve("registry").resolve("${agentName.substring(0, 1)}-agents.yaml")
        val agentInfoList: List<AgentInfo> =
            Yaml.decodeFromString(ListSerializer(AgentInfo.serializer()), agentRegistryFile.readText())
        return agentInfoList.first { it.name.lowercase() == agentName.lowercase() }
    }

    fun registerAgent(agentInfo: AgentInfo) {
        val agentRegistryFile = projectDir.resolve("registry").resolve("${agentInfo.name.substring(0, 1)}-agents.yaml")
        val agentInfoList: MutableList<AgentInfo> = try {
            Yaml.decodeFromString(ListSerializer(AgentInfo.serializer()), agentRegistryFile.readText()).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
        agentInfoList.add(agentInfo)
        agentRegistryFile.writeText(Yaml.encodeToString(ListSerializer(AgentInfo.serializer()), agentInfoList))
    }

}