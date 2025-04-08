package org.eclipse.lmos.cli.commands.agent

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.cli.Conversation
import org.eclipse.lmos.cli.InputContext
import org.eclipse.lmos.cli.SystemContext
import org.eclipse.lmos.cli.UserContext
import org.eclipse.lmos.cli.agent.AgentManager
import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.arc.ArcAgentClientService
import org.eclipse.lmos.cli.factory.AgentManagerFactory
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.llm.LLMConfig
import org.eclipse.lmos.cli.registry.agent.AgentRegistry
import org.eclipse.lmos.cli.utils.CliPrinter.printConvOutput
import org.eclipse.lmos.cli.utils.CliPrinter.printError
import org.eclipse.lmos.cli.utils.CliPrinter.printSuccess
import org.eclipse.lmos.cli.utils.CliPrinter.printlnHeader
import picocli.CommandLine
import java.util.*
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.emptyMap
import kotlin.collections.forEach
import kotlin.collections.mapNotNull
import kotlin.collections.mutableListOf

@CommandLine.Command(name = "chat", description = ["Chat with the system"], mixinStandardHelpOptions = true)
class Chat : Runnable {

    @CommandLine.Option(names = ["--an"], description = ["Agent name"])
    private var agentName: String? = null

    private val history = mutableListOf<Pair<String, String>>()

    override fun run() {

        val agentName: String = agentName ?: promptUser("Enter the agent name")

        val agentRegistry = AgentRegistry()
        val agentInfo: AgentInfo =
            agentRegistry.findAgent(agentName) ?: run {
                printError("Agent $agentName not found")
                return
            }

        val startStatus = runBlocking { startProject() }

        if(startStatus) {
            chatLoop(agentInfo)
        } else {
            val logs = getLogs(agentInfo)
            printError("Failed to start agent")
            logs.forEach { printlnHeader(it) }
        }


    }

    private fun getLogs(agentInfo: AgentInfo): List<String> {
        val agentManager: AgentManager = AgentManagerFactory.agentManager()
        return agentManager.getLogs(agentInfo)
    }

    private fun chatLoop(agentInfo: AgentInfo) {
        val conversationId = UUID.randomUUID().toString()
        val messages = mutableListOf<Message>()
        val systemContext = SystemContext("channelId")
        val userContext = UserContext("userId", "userToken")

        while (true) {
            val input = promptUser("User query:")
            if (input.equals("/bye", ignoreCase = true)) {
                shutdownAgent(agentInfo)
                printSuccess("Agent chat session closed. See you next time!")
                break
            }

            val turnId = UUID.randomUUID().toString()
            messages.add(Message(role = "user", content = input, turnId = turnId))

            var response = ""
            try {
                runBlocking {
                    ArcAgentClientService().askAgent(
                        Conversation(InputContext(messages), systemContext, userContext),
                        conversationId, turnId, agentInfo.name, "localhost"
                    ).collect {
                        response = it
                    }
                }
                history.add(input to response)
                printConvOutput("${agentInfo.name}:", response)
            } catch (e: Exception) {
                printError("An error occurred while communicating with the agent: ${e.message}")
                shutdownAgent(agentInfo)
                break
            }
        }
    }

    private fun shutdownAgent(agentInfo: AgentInfo) {
        val agentManager: AgentManager = AgentManagerFactory.agentManager()
        agentManager.shutdownAgent(agentInfo)
    }

    private fun startProject(): Boolean {
        val defaultLLMConfigManager = DefaultLLMConfigManager()
        val llmConfigs: List<LLMConfig> = defaultLLMConfigManager.listLLMConfig().mapNotNull {
            defaultLLMConfigManager.getLLMConfig(it)
        }
        val agentManager: AgentManager = AgentManagerFactory.agentManager()
        return agentManager.startAgent(llmConfigs) == AgentStatus.READY
    }
}

enum class AgentStatus {
    STARTING, READY, FAILED, ERROR
}

@Serializable
data class AgentInfo(
    val name: String,
    val type: AgentType,
    val id: String? = null,
    val protocol: String? = null,
    val directory: String? = null,
    val connectionParams: Map<String, String>? = emptyMap()
)
