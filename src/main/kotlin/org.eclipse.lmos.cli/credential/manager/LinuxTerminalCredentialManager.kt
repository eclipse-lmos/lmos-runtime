//package org.eclipse.lmos.cli.credential.manager
//
//import org.eclipse.lmos.cli.credential.Credential
//import org.eclipse.lmos.cli.credential.CredentialManagerType
//
//
//class LinuxTerminalCredentialManager : CredentialManager {
//    override fun credentialManagerType() = CredentialManagerType.LINUX
//
//    override fun testCredentialManager(): Boolean {
//        return try {
//            val testTarget = "CredentialManagerTestTarget"
//            val user = "testUser4"
//            val testPassword = "testPassword"
//
//            // Add a test credential
//            val addCommand = arrayOf(
//                "secret-tool",
//                "store",
//                "--label=${testTarget}${user}",
//                "target", testTarget,
//                "user", user
//            )
//            executeCommand(addCommand, testPassword)
//
//            // Retrieve the test credential to verify it was stored
//            val retrievedCredential = getCredential(testTarget, user)
//            if (retrievedCredential == null || retrievedCredential.content != testPassword) {
//                throw Exception("Credential retrieval failed")
//            }
//
//            // Delete the test credential
//            val deleteCommand = arrayOf(
//                "secret-tool",
//                "clear",
//                "target", testTarget,
//                "user", user
//            )
//            executeCommand(deleteCommand)
//
//            true
//        } catch (e: Exception) {
//            println("testCredentialManager exception: $e")
//            false
//        }
//    }
//
//    override fun addCredential(prefix: String, credential: Credential) {
//        val label = "$prefix${credential.id}"
//        val command = arrayOf(
//            "secret-tool",
//            "store",
//            "--label=$label",
//            "target", prefix,
//            "user", credential.id
//        )
//        executeCommand(command, credential.content)
//    }
//
//    override fun getCredential(prefix: String, id: String): Credential? {
//        val command = arrayOf(
//            "secret-tool",
//            "lookup",
//            "target", prefix,
//            "user", id
//        )
//        val output = executeCommand(command).trim()
//        return if (output.isNotEmpty()) {
//            Credential(id, output)
//        } else {
//            null
//        }
//    }
//
//    override fun updateCredential(prefix: String, credential: Credential) {
//        // Updating is effectively the same as adding in this context
//        addCredential(prefix, credential)
//    }
//
//    override fun deleteCredential(prefix: String, id: String) {
//        val command = arrayOf(
//            "secret-tool",
//            "clear",
//            "target", prefix,
//            "user", id
//        )
//        executeCommand(command)
//    }
//
//    override fun listCredentials(prefix: String): Set<Credential> {
//        val command = arrayOf(
//            "secret-tool",
//            "search",
//            "target", prefix
//        )
//        val output = executeCommand(command).trim()
//        val credentials = mutableSetOf<Credential>()
//
//        if (output.isNotEmpty()) {
//            // Parse the output to extract user IDs
//            val entries = output.split("\n\n") // Separate entries by double newlines
//            for (entry in entries) {
//                val lines = entry.lines()
//                var userId: String? = null
//                for (line in lines) {
//                    if (line.startsWith("attribute.user = ")) {
//                        userId = line.substringAfter("attribute.user = ").trim()
//                        break
//                    }
//                }
//                if (userId != null) {
//                    val credential = getCredential(prefix, userId)
//                    if (credential != null) {
//                        credentials.add(credential)
//                    }
//                }
//            }
//        }
//        return credentials
//    }
//
//    override fun deleteAllCredentials(prefix: String) {
//        TODO("Not yet implemented")
//    }
//
//    fun executeCommand(command: Array<String>, input: String? = null, wait: Boolean = true): String {
//        val processBuilder = ProcessBuilder(*command).redirectErrorStream(true)
//        val process = processBuilder.start()
//
//        // Write input to the process's stdin if input is provided
//        if (input != null) {
//            process.outputStream.use { outputStream ->
//                outputStream.write(input.toByteArray())
//                outputStream.flush()
//            }
//        }
//
//        // Read the process's stdout
//        val output = StringBuilder()
//        if (wait) {
//            process.inputStream.bufferedReader().use { reader ->
//                output.append(reader.readText())
//            }
//            process.waitFor()
//        }
//
//        return output.toString()
//    }
//}
