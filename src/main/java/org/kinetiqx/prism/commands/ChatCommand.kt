package org.kinetiqx.prism.commands

import picocli.CommandLine

@CommandLine.Command(name = "chat", description = ["Chat with the system"], mixinStandardHelpOptions = true)
class ChatCommand : Runnable {
    @CommandLine.Parameters(index = "0", arity = "1..*", description = ["User query string"])
    private lateinit var userQuery: Array<String>

    override fun run() {
        val query = java.lang.String.join(" ", *userQuery)
        println("Chatting with query: $query")
        // Implement the logic to handle the chat query here
    }
}