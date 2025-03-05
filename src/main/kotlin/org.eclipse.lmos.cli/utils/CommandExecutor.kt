package org.eclipse.lmos.cli.utils

import java.io.BufferedReader
import java.io.File
import java.util.concurrent.TimeUnit


fun executeCommand(command: Array<String>, wait: Boolean = true): Pair<Long, String> {
    val process = ProcessBuilder(*command).redirectErrorStream(true).start()
    var output = ""
    if(wait) {
        output = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
    }
    return Pair(process.pid(), output)
}

fun executeCommandStreaming(command: Array<String>, timeoutSeconds: Long, logs: MutableList<String>) {
    val process = ProcessBuilder(*command).redirectErrorStream(true).start()
    val reader = process.inputStream.bufferedReader()

    reader.useLines { lines ->
        for (line in lines) {
            println("Streaming log: $line")
            logs.add(line)
        }
    }
    process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
}


fun executeCommand(
    command: List<String>,
    envVars: Map<String, String>,
    workingDir: File,
    wait: Boolean = true
): Pair<Long, String>  {
    val processBuilder = ProcessBuilder(command)
        .directory(workingDir)
        .redirectErrorStream(true)
        .redirectOutput(ProcessBuilder.Redirect.appendTo(File(workingDir, "application.log")))

    // Add environment variables
    val environment = processBuilder.environment()
    environment.putAll(envVars)

    val process = processBuilder.start()

    var output = ""
    if (wait) {
        output = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
    }

    return Pair(process.pid(), output)
}