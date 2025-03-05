package org.eclipse.lmos.cli.commands.agent

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.cli.*
import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.credential.manager.executeCommand
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.llm.LLMConfig
import org.eclipse.lmos.cli.registry.agent.AgentRegistry
import org.eclipse.lmos.cli.starter.AgentStarter
import org.eclipse.lmos.cli.starter.AgentStarterFactory
import picocli.CommandLine
import java.io.*
import java.nio.file.Path
import java.util.*
import kotlin.collections.List
import kotlin.io.path.exists
import kotlin.io.path.readText

@CommandLine.Command(name = "chat", description = ["Chat with the system"], mixinStandardHelpOptions = true)
class Chat : Runnable {

    @CommandLine.Option(names = ["--an"], description = ["Agent name"])
    private var agentName: String? = null

    private val history = mutableListOf<Pair<String, String>>()  // Stores input-response pairs

    override fun run() {

        println("Chatting with query: $agentName")

        val agentName: String = agentName ?: promptUser("Enter the agent name")

        agentName.let {

            val conversationId = UUID.randomUUID().toString()
            val startStatus = runBlocking { startProject() }
            val message = listOf<Message>()
            val systemContext = SystemContext("channelId")
            val userContext = UserContext("userId", "userToken")

            while (true) {


                val inputContext = InputContext(message)
                val conversation = Conversation(inputContext, systemContext, userContext)

                if(startStatus.uppercase() == "STARTED") {

                    val agentRegistry = AgentRegistry()
//                val agentInfo: AgentInfo = agentRegistry.findAgent(it)

                    val input =
//                    "hi"
                        promptUser("Enter your query")
                    val turnId = UUID.randomUUID().toString()

                    message.plus(Message(role = "user", content = input, turnId = turnId))

                    var response = ""

                    runBlocking {
                        ArcAgentClientService().askAgent(
                            conversation, conversationId, turnId,
                            agentName, "localhost"
                        ).collect { it ->
                            println("Agent Response: $it")
                            response = it
                        }

                    }

//                try {
//                    graphQlAgentClient.callAgent(
//                        agentRequest,
//                        requestHeaders = subsetHeader,
//                    ).collect { response ->
//                        log.info("Agent Response: $response")
//                        emit(
//                            AssistantMessage(
//                                response.messages[0].content,
//                                response.anonymizationEntities,
//                            ),
//                        )
//                    }

                    if (input.equals("exit", ignoreCase = true)) {
                        println("Exiting session...")
                        break
                    }

//                val response = processInput(input, agentInfo)  //todo agent-integrator
                    history.add(input to response)
                    println("Response: $response")
                } else {
                    println("Error in starting Agents, consult logs")
                }
            } } ?: TODO("Implement Direct LLM integration") //todo integrate llm call


    }

    private fun startProject(): String {
        val projectDirectory = AGENT_PROJECTS_DIRECTORY.resolve(AgentType.ARC.name)



        val agentStarter: AgentStarter = AgentStarterFactory().getAgentStarter()

        println("agentStarter type: ${agentStarter.javaClass}")

        val startAgent = agentStarter.startAgent()

        val args = arrayOf("")  //todo fetch config from credential manager

        val command = "cd $projectDirectory && nohup ./gradlew -q --console=plain bootrun --args='$args'>application.log 2>&1 < /dev/null &"   //macos

        val windowsCommand = "cd $projectDirectory && start /b /min \"\" gradlew -q --console=plain bootrun --args=\"$args\" > application.log 2>&1"

        //todo execute command and return result

        return startAgent
    }

    private fun processInput(input: String, agentInfo: AgentInfo): String {

        val agentCommunicator: AgentCommunicator? = null
        agentCommunicator?.connect()
        if(AgentStatus.READY == agentCommunicator?.checkStatus()) {
            agentCommunicator.sendCommand(input)
        }
        agentCommunicator?.disconnect()

        return "Answer: $input"
    }
}

interface AgentDiscovery {

    fun discoverAgents(): List<AgentInfo>

}

interface AgentCommunicator {
    fun connect()
    fun checkStatus(): AgentStatus
    fun sendCommand(command: String): String
    fun disconnect()
}

enum class AgentStatus {
    STARTING, READY, FAILED
}


class ConsoleIOAdapter(private val processBuilder: ProcessBuilder) : AgentCommunicator {
    private var process: Process? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null

    override fun connect() {
        process = processBuilder.start()
        reader = BufferedReader(InputStreamReader(process?.inputStream))
        writer = BufferedWriter(OutputStreamWriter(process?.outputStream))
    }

    override fun checkStatus(): AgentStatus {
        TODO("Not yet implemented")
    }

    override fun sendCommand(command: String): String {
        writer?.write(command)
        writer?.newLine()
        writer?.flush()
        return reader?.readLine() ?: ""
    }

    override fun disconnect() {
        reader?.close()
        writer?.close()
        process?.destroy()
    }
}

@Serializable
data class AgentInfo(
    val id: String,
    val name: String,
    val protocol: String,
    val directory: String?,
    val connectionParams: Map<String, String>?
)
