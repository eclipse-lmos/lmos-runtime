package org.eclipse.lmos.cli.credential.manager

import kotlinx.serialization.builtins.ListSerializer
import net.mamoe.yamlkt.Yaml
import org.eclipse.lmos.cli.constants.LmosCliConstants.CredentialManagerConstants.CREDENTIAL_CONFIG
import org.eclipse.lmos.cli.constants.LmosCliConstants.CredentialManagerConstants.CREDENTIAL_DIRECTORY
import org.eclipse.lmos.cli.credential.Credential
import org.eclipse.lmos.cli.credential.CredentialManagerType
import org.eclipse.lmos.cli.utils.EncryptionUtils
import java.nio.file.Files
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class DefaultCredentialManager: CredentialManager {

    private val keyFile = CREDENTIAL_DIRECTORY.resolve("key").toFile()
    private val key: SecretKey
    private val credentials: MutableMap<String, Credential> = mutableMapOf()

    init {
        key = loadOrCreateKey()
        loadCredentials()
    }

    override fun credentialManagerType() = CredentialManagerType.DEFAULT

    override fun testCredentialManager() = true

    private fun loadOrCreateKey(): SecretKey {
        if (keyFile.exists()) {
            val keyBytes = Files.readAllBytes(keyFile.toPath())
            return SecretKeySpec(keyBytes, "AES")
        } else {
            val newKey = EncryptionUtils.generateKey()
            val keyBytes = newKey.encoded
            Files.write(keyFile.toPath(), keyBytes)
            keyFile.setWritable(true, true)
            return newKey
        }
    }

    private fun loadCredentials() {
            val encryptedData = Files.readAllBytes(CREDENTIAL_CONFIG.toPath())
            val jsonData = EncryptionUtils.decrypt(encryptedData, key)
            val creds: List<Credential> = Yaml.decodeFromString(ListSerializer(Credential.serializer()), String(jsonData))
            for (cred in creds) {
                credentials[cred.id] = cred
            }
    }

    private fun saveCredentials() {
        val credsList = credentials.values.toList()
        val jsonData = Yaml.encodeToString(credsList)
        val encryptedData = EncryptionUtils.encrypt(jsonData.toByteArray(), key)
        Files.write(CREDENTIAL_CONFIG.toPath(), encryptedData)
    }

    override fun addCredential(prefix: String, credential: Credential) {
        credentials[credential.id] = credential
        saveCredentials()
    }

    override fun getCredential(prefix: String, id: String): Credential? {
        return credentials[id]
    }

    override fun updateCredential(prefix: String, credential: Credential) {
        credentials[credential.id] = credential
        saveCredentials()
    }

    override fun deleteCredential(prefix: String, id: String) {
        credentials.remove(id)
        saveCredentials()
    }

    override fun listCredentials(prefix: String, ): Set<Credential> {
        return credentials.values.toSet()
    }

    override fun deleteAllCredentials(prefix: String, ) {
        credentials.clear()
        CREDENTIAL_CONFIG.delete()
    }
} 