package org.eclipse.lmos.cli.commands.agent

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.cli.*
import org.eclipse.lmos.cli.agent.AgentType
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.credential.CredentialManagerType
import org.eclipse.lmos.cli.credential.manager.executeCommand
import org.eclipse.lmos.cli.factory.getOS
import org.eclipse.lmos.cli.llm.DefaultLLMConfigManager
import org.eclipse.lmos.cli.llm.LLMConfig
import org.eclipse.lmos.cli.registry.agent.AgentRegistry
import picocli.CommandLine
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.Arrays
import java.util.UUID
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

interface AgentStarter {

    fun startAgent(): String
    fun checkStatus(): AgentStatus
}

class AgentStarterFactory {
    fun getAgentStarter(): AgentStarter {
        val os = getOS()
        return when(os) {
            CredentialManagerType.MAC -> MacOSAgentStarter()
            CredentialManagerType.WIN -> WindowsAgentStarter()
            CredentialManagerType.LINUX -> MacOSAgentStarter()
            CredentialManagerType.DEFAULT -> LinuxAgentStarter()
        }
    }

}

class LinuxAgentStarter : AgentStarter {
    override fun startAgent(): String {
        TODO("Not yet implemented")
    }

    override fun checkStatus(): AgentStatus {
        TODO("Not yet implemented")
    }

}

//class WindowsAgentStarter : AgentStarter {
//    override fun startAgent(): String {
//
//        val agents = AGENT_PROJECTS_DIRECTORY.resolve(AgentType.ARC.name)
//
//        //        val deleteCommand = "cd $projectDirectory && if [ -f application.log ]; then rm application.log && echo \"deleted\"; fi"
//        val deleteCommand = arrayOf("cmd.exe", "/c", "cd /d $agents && if exist application.log (del application.log && echo deleted)")
//        executeCommand(deleteCommand)
//
////        val startCommand = "cd $projectDirectory && nohup ./gradlew -q --console=plain bootrun --args='$params'>application.log 2>&1 < /dev/null &"
//
////        actual fun chatCommand(directory: String, agentName: String): String = "cd $projectDirectory && ./gradlew -q --console=plain arc -Pagent=$agentName"
//
//        val params = "--management.endpoint.shutdown.enabled=true --management.endpoints.web.exposure.include=\"*\""
//
//        val defaultLLMConfigManager = DefaultLLMConfigManager()
//        val llmConfigs: List<LLMConfig> = defaultLLMConfigManager.listLLMConfig().mapNotNull {
//            defaultLLMConfigManager.getLLMConfig(it)
//        }.toList()
//
//
//        val envVars = llmConfigs.mapIndexed { index, config ->
//            """
//        set ARC_AI_CLIENTS_${index}_ID=${config.id}
//        set ARC_AI_CLIENTS_${index}_CLIENT=${config.provider}
//        set ARC_AI_CLIENTS_${index}_URL=${config.baseUrl}
//        set ARC_AI_CLIENTS_${index}_APIKEY=${config.apiKey}
//        set ARC_AI_CLIENTS_${index}_MODELNAME=${config.modelName}
//        """.trimIndent()
//        }.joinToString("\n")
//
//        val startCommand = arrayOf(
//            "cmd.exe", "/c", """
//    cd /d $agents &&
//    ${envVars.replace("\n", " && ")} &&
//    start /b /min "" gradlew -q --console=plain bootrun --args="$params" > application.log 2>&1
//    """.trimIndent()
//        )
//
//        run {val result = executeCommand(startCommand, false)
//            println("Command: ${startCommand.contentToString()}, Result: $result")
//        }
//
//        return runAtFixedRate(setOf("STARTED", "FAILED"), 2000, 30, 4000) { checkForStartUpStatus(agents) }
//
//
//    }
//
//    override fun checkStatus(): AgentStatus {
//        TODO("Not yet implemented")
//    }
//}

class WindowsAgentStarter : AgentStarter {
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

class MacOSAgentStarter: AgentStarter {
    override fun startAgent(): String {

        val agents = AGENT_PROJECTS_DIRECTORY.resolve(AgentType.ARC.name)

        //        val deleteCommand = "cd $projectDirectory && if [ -f application.log ]; then rm application.log && echo \"deleted\"; fi"
        val deleteCommand = arrayOf("sh", "-c", "cd $agents && if [ -f application.log ]; then rm application.log && echo \"deleted\"; fi")
        val result = executeCommand(deleteCommand)
        println("Delete command: ${deleteCommand.contentToString()}, Result: $result")

//        val startCommand = "cd $projectDirectory && nohup ./gradlew -q --console=plain bootrun --args='$params'>application.log 2>&1 < /dev/null &"

//        actual fun chatCommand(directory: String, agentName: String): String = "cd $projectDirectory && ./gradlew -q --console=plain arc -Pagent=$agentName"

        val params = "--management.endpoint.shutdown.enabled=true --management.endpoints.web.exposure.include=\"*\""

        val defaultLLMConfigManager = DefaultLLMConfigManager()
        val llmConfigs: List<LLMConfig> = defaultLLMConfigManager.listLLMConfig().mapNotNull {
            defaultLLMConfigManager.getLLMConfig(it)
        }.toList()


        val envVars = llmConfigs.mapIndexed { index, config ->
            """
        export ARC_AI_CLIENTS_${index}_ID=${config.id}
        export ARC_AI_CLIENTS_${index}_CLIENT=${config.provider}
        export ARC_AI_CLIENTS_${index}_URL=${config.baseUrl}
        export ARC_AI_CLIENTS_${index}_APIKEY=${config.apiKey}
        export ARC_AI_CLIENTS_${index}_MODELNAME=${config.modelName}
        """.trimIndent()
        }.joinToString("\n")

        val startCommand = arrayOf(
            "sh", "-c", """
        cd $agents &&
        $envVars &&
        nohup ./gradlew -q --console=plain bootrun --args='$params' > application.log 2>&1 < /dev/null &
        """.trimIndent()
        )

        println("Start Command: ${startCommand.contentToString()}")
        run {executeCommand(startCommand, false)
            println("Completed Command: ${startCommand.contentToString()}")
        }

        return runAtFixedRate(setOf("STARTED", "FAILED"), 2000, 30, 4000) { checkForStartUpStatus(agents) }
    }

    override fun checkStatus(): AgentStatus {
        TODO("Not yet implemented")
    }
}

private fun checkForStartUpStatus(projectRootDirectory: Path): String {

    println("checking for status")
    val logFile = projectRootDirectory.resolve("application.log")

    val s = when {
        logFile.exists() -> {
            val logs = logFile.readText()
            when {
                "Starting hot-reload of agents" in logs -> "STARTED"
                "finished with non-zero exit value 1" in logs -> "FAILED"
                else -> "PROGRESS"
            }
        }

        else -> "PROGRESS"
    }
    println("result of check status $s")
    return s
}

private fun runAtFixedRate(stopKeywords: Set<String>, pollingDurationMillis: Long, maxAttempts: Long, initialDelayMillis: Long = 0, fn: () -> String): String {
    println("Polling agent status")
    var matched = ""
    runBlocking {
        var i = 0
        delay(initialDelayMillis)
        while (matched.isEmpty() && i++ < maxAttempts) {
            delay(pollingDurationMillis)
            val result = fn()
            println("result: $result")
            matched = stopKeywords.find { result.contains(it) } ?: ""
        }
    }
    return matched
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
