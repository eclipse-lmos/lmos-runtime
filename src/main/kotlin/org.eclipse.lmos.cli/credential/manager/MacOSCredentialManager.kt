package org.eclipse.lmos.cli.credential.manager

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.lmos.cli.credential.Credential
import org.eclipse.lmos.cli.credential.CredentialManagerType
import java.io.BufferedReader
import java.util.*


interface CredentialManager {

    fun credentialManagerType(): CredentialManagerType
    fun testCredentialManager(): Boolean
    fun addCredential(prefix: String, credential: Credential)
    fun getCredential(prefix: String, id: String): Credential?
    fun updateCredential(prefix: String, credential: Credential)
    fun deleteCredential(prefix: String, id: String)
    fun listCredentials(prefix: String): Set<Credential>
    fun deleteAllCredentials(prefix: String)

}

@ApplicationScoped
class MacOSCredentialManager: CredentialManager {

    override fun credentialManagerType() = CredentialManagerType.MAC

    override fun testCredentialManager(): Boolean {
        val command = arrayOf("security", "list-keychains")
        val output = executeCommand(command)
        return output.contains("keychain") && !output.contains("unknown command")
    }

    override fun addCredential(prefix: String, credential: Credential) {
        val serviceName = "$prefix${credential.id}"
        val command = arrayOf(
            "security",
            "add-generic-password",
            "-s",
            serviceName,
            "-a",
            credential.id,
            "-w",
            Base64.getEncoder().encodeToString(credential.content.toByteArray(Charsets.UTF_8)),
            "-C",
            prefix.substring(0, 4),
            "-U" // Update if exists
            // Optionally, add a comment or label to help identify entries
            // "-j", "ManagedByCredentialManagerApp"
        )
        executeCommand(command)
    }

    override fun getCredential(prefix: String, id: String): Credential? {
        val serviceName = "$prefix$id"
        val command = arrayOf("sh", "-c", "security find-generic-password -s '$serviceName' -w")
        val output = executeCommand(command)
        if (output.contains("could not be found")) {
            return null
        }
//        val cleanedOutput = output.replace("\\s".toRegex(), "")
        return Credential(id, String(Base64.getMimeDecoder().decode(output), Charsets.UTF_8))
    }

    override fun updateCredential(prefix: String, credential: Credential) {
        // The add-generic-password command with the -U option updates the credential if it exists
        addCredential(prefix, credential)
    }

    override fun deleteCredential(prefix: String, id: String) {
        val serviceName = "$prefix$id"
        val command = arrayOf("security", "delete-generic-password", "-s", serviceName)
        executeCommand(command)
    }

    override fun listCredentials(prefix: String): Set<Credential> {
        val output = executeCommand(listOf("security", "dump-keychain").toTypedArray())

        return output.lines()
            .filter { it.contains(prefix) }
            .map {
                val id = it.substringAfter(prefix).substringBefore("\"")
                Credential(id = id, content = "")
            }
            .toSet()
    }


    override fun deleteAllCredentials(prefix: String) {
        val credentials = listCredentials(prefix)
        for (credential in credentials) {
            deleteCredential(prefix, credential.id)
        }
    }

    private fun executeCommand(command: Array<String>): String {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
        return output
    }

}
