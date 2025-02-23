package org.kinetiqx.prism.credentials.store

import jakarta.enterprise.context.ApplicationScoped
import org.kinetiqx.prism.credentials.Credential
import java.io.BufferedReader

@ApplicationScoped
class CredentialStore {

    private val servicePrefix = "CredentialManagerApp:"

    fun addCredential(credential: Credential) {
        val serviceName = "$servicePrefix${credential.id}"
        val command = arrayOf(
            "security",
            "add-generic-password",
            "-s",
            serviceName,
            "-a",
            credential.id,
            "-w",
            credential.content,
            "-C",
            servicePrefix.substring(0, 4),
            "-U" // Update if exists
            // Optionally, add a comment or label to help identify entries
            // "-j", "ManagedByCredentialManagerApp"
        )
        executeCommand(command)
    }

    fun getCredential(id: String): Credential? {
        val serviceName = "$servicePrefix$id"
        val command = arrayOf("security", "find-generic-password", "-s", serviceName, "-g")
        val output = executeCommand(command)
        if (output.contains("could not be found")) {
            return null
        }
        val passwordRegex = Regex("password:\\s+\"(.*?)\"")
        val passwordMatch = passwordRegex.find(output)
        val password = passwordMatch?.groupValues?.get(1)
        return if (password != null) {
            Credential(id, password)
        } else {
            null
        }
    }

    fun updateCredential(credential: Credential) {
        // The add-generic-password command with the -U option updates the credential if it exists
        addCredential(credential)
    }

    fun deleteCredential(id: String) {
        val serviceName = "$servicePrefix$id"
        val command = arrayOf("security", "delete-generic-password", "-s", serviceName)
        executeCommand(command)
    }

    fun listCredentials(): Set<String> {
        val output = executeCommand(listOf("security", "dump-keychain").toTypedArray())

        return output.lines()
            .filter { it.contains(servicePrefix) }
            .map { it.substringAfter(servicePrefix).substringBefore("\"") }.toSet()
    }

    fun deleteAllCredentials() {
        val credentials = listCredentials()
        for (credential in credentials) {
            deleteCredential(credential)
        }
    }

    private fun executeCommand(command: Array<String>): String {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
        return output
    }

}