package org.kinetiqx.prism.credentials.store
  
import jakarta.enterprise.context.ApplicationScoped
import org.kinetiqx.prism.credentials.Credential
import java.io.BufferedReader  
  
@ApplicationScoped  
class LinuxCredentialStore {  
  
    private val servicePrefix = "CredentialManagerApp:"

    fun addCredential(credential: Credential) {
        val serviceName = "$servicePrefix${credential.id}"
        val command = arrayOf(
            "sh", "-c",
            "echo '${credential.content}' | secret-tool store --label=$serviceName service $serviceName username ${credential.id}"
        )
        executeCommand(command)
    }
  
    fun getCredential(id: String): Credential? {
        val serviceName = "$servicePrefix$id"  
        val command = arrayOf(  
            "secret-tool",  
            "lookup",  
            "service", serviceName,  
            "username", id  
        )  
        val password = executeCommand(command).trim()  
        return if (password.isNotEmpty()) {  
            Credential(id, password)
        } else {  
            null  
        }  
    }  
  
    fun updateCredential(credential: Credential) {
        deleteCredential(credential.id)  
        addCredential(credential)  
    }  
  
    fun deleteCredential(id: String) {  
        val serviceName = "$servicePrefix$id"  
        val command = arrayOf(  
            "secret-tool",  
            "clear",  
            "service", serviceName,  
            "username", id  
        )  
        executeCommand(command)  
    }  
  
    fun listCredentials(): Set<String> {  
        val command = arrayOf("secret-tool", "search", "service", servicePrefix.trimEnd(':'))  
        val output = executeCommand(command)  
  
        val ids = mutableSetOf<String>()  
        val regex = Regex("label:\\s*$servicePrefix(\\S+)")  
        regex.findAll(output).forEach { matchResult ->  
            val id = matchResult.groupValues[1]  
            ids.add(id)  
        }  
        return ids  
    }  
  
    fun deleteAllCredentials() {  
        val credentials = listCredentials()  
        for (id in credentials) {  
            deleteCredential(id)  
        }  
    }  
  
    private fun executeCommand(command: Array<String>): String {  
        val process = ProcessBuilder(*command)  
            .redirectErrorStream(true)  
            .start()  
        val output = process.inputStream.bufferedReader().use(BufferedReader::readText)  
        process.waitFor()  
        return output  
    }  
  
    private fun executeCommandWithInput(command: Array<String>, input: String) {
        val process = ProcessBuilder(*command)  
            .redirectErrorStream(true)  
            .start()  
        process.outputStream.bufferedWriter().use { writer ->  
            writer.write(input)  
            writer.flush()  
        }  
        process.waitFor()  
    }  
}  