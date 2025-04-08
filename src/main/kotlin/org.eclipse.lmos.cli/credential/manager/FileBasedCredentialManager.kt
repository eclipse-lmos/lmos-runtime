package org.eclipse.lmos.cli.credential.manager

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.serialization.builtins.ListSerializer
import net.mamoe.yamlkt.Yaml
import org.eclipse.lmos.cli.constants.LmosCliConstants.CredentialManagerConstants.CREDENTIAL_DIRECTORY
import org.eclipse.lmos.cli.credential.Credential
import org.eclipse.lmos.cli.credential.CredentialManagerType
import org.eclipse.lmos.cli.utils.EnvUtils
import org.slf4j.LoggerFactory
import java.util.*

@ApplicationScoped
class FileBasedCredentialManager : CredentialManager {

    private val log = LoggerFactory.getLogger(FileBasedCredentialManager::class.java)

    private val encryption = SecureStringEncryption()
    private val yaml = Yaml()
    private val credentialSerializer = ListSerializer(Credential.serializer())

    override fun credentialManagerType() = CredentialManagerType.MAC

    override fun testCredentialManager() = true

    override fun addCredential(prefix: String, credential: Credential) {
        log.info("Adding credential with id {} for prefix {}", credential.id, prefix)
        val credentials = listCredentials(prefix).toMutableSet()
        val encryptedContent = encryption.encrypt(credential.content, EnvUtils.getLmosCliSecretKey())
        val encryptedCredential = credential.copy(
            content = Base64.getEncoder().encodeToString(encryptedContent)
        )
        val existingRemoved = credentials.removeIf { it.id == credential.id } // Replace existing credential with same ID
        if (existingRemoved) {
            log.info("Replaced existing credential with id {} for prefix {}", credential.id, prefix)
        } else {
            log.info("Adding new credential with id {} for prefix {}", credential.id, prefix)
        }
        credentials.add(encryptedCredential)
        saveCredentials(prefix, credentials)
        log.info("Credential with id {} for prefix {} added successfully", credential.id, prefix)
    }

    override fun listCredentials(prefix: String): Set<Credential> {
        val file = credentialFile(prefix)
        if (!file.exists()) {
            log.info("No credential file found for prefix {}. Returning empty credential set.", prefix)
            return setOf()
        }

        val content = file.readText()
        try {
            val credentials = yaml.decodeFromString(credentialSerializer, content).toSet()
            log.info("Credentials for prefix {} listed successfully", prefix)
            return credentials
        } catch (e: Exception) {
            log.error("Error decoding credentials from file for prefix {}: {}", prefix, e.message)
            throw e
        }
    }

    override fun getCredential(prefix: String, id: String): Credential? {
        val credential = listCredentials(prefix).find { it.id == id } ?: return null
        try {
            val decryptedContent = encryption.readAndDecrypt(
                EnvUtils.getLmosCliSecretKey(),
                Base64.getDecoder().decode(credential.content)
            )
            log.info("Credential with id {} for prefix {} retrieved successfully", id, prefix)
            return credential.copy(content = decryptedContent)
        } catch (e: Exception) {
            log.error("Error decrypting credential with id {} for prefix {}: {}", id, prefix, e.message)
            throw e
        }
    }

    override fun updateCredential(prefix: String, credential: Credential) {
        log.info("Updating credential with id {} for prefix {}", credential.id, prefix)
        addCredential(prefix, credential)
    }

    override fun deleteCredential(prefix: String, id: String) {
        val credentials = listCredentials(prefix).toMutableSet()
        val removed = credentials.removeIf { it.id == id }
        if (removed) {
            if (credentials.isEmpty()) {
                credentialFile(prefix).delete()
                log.info("No more credentials for prefix {}. Credential file deleted.", prefix)
            } else {
                saveCredentials(prefix, credentials)
                log.info("Credential with id {} for prefix {} deleted successfully", id, prefix)
            }
        } else {
            log.warn("Credential with id {} not found for prefix {}", id, prefix)
        }
    }

    override fun deleteAllCredentials(prefix: String) {
        log.info("Deleting all credentials for prefix {}", prefix)
        val file = credentialFile(prefix)
        if (file.exists()) {
            file.delete()
            log.info("Credential file for prefix {} deleted successfully", prefix)
        } else {
            log.warn("Credential file for prefix {} does not exist", prefix)
        }
    }

    private fun saveCredentials(prefix: String, credentials: Set<Credential>) {
        try {
            val configYaml = yaml.encodeToString(credentialSerializer, credentials.toList())
            credentialFile(prefix).writeText(configYaml)
            log.info("Credentials for prefix {} saved successfully", prefix)
        } catch (e: Exception) {
            log.error("Error saving credentials for prefix {}: {}", prefix, e.message)
            throw e
        }
    }

    private fun credentialFile(prefix: String) =
        CREDENTIAL_DIRECTORY.resolve(".secret-${prefix}.yaml").toFile()
}



