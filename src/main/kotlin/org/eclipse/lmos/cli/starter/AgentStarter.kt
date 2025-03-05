package org.eclipse.lmos.cli.starter

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.cli.commands.agent.AgentStatus
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

interface AgentStarter {

    fun startAgent(): String
    fun checkStatus(): AgentStatus
}

fun checkForStartUpStatus(projectRootDirectory: Path): String {

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

fun runAtFixedRate(stopKeywords: Set<String>, pollingDurationMillis: Long, maxAttempts: Long, initialDelayMillis: Long = 0, fn: () -> String): String {
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