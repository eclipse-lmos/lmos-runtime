package org.kinetiqx.prism.credentials

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.quarkus.runtime.annotations.RegisterForReflection
import org.kinetiqx.prism.credentials.utils.EncryptionUtils
import java.nio.file.Files
import java.nio.file.Paths
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@RegisterForReflection
data class Credential(
    val id: String,
//    val username: String,
    val content: String,
)

//interface CredentialStore {
//    fun addCredential(credential: Credential)
//
//    fun getCredential(id: String): Credential?
//
//    fun updateCredential(credential: Credential)
//
//    fun deleteCredential(id: String)
//
//    fun listCredentials(): List<Credential>
//
//    fun deleteAllCredentials()
//}

class DefaultCredentialStore {
    private val homeDir = System.getProperty("user.home")
    private val credDir = Paths.get(homeDir, ".credman")
    private val credFile = credDir.resolve("credentials.json").toFile()
    private val keyFile = credDir.resolve("key").toFile()
    private val objectMapper = jacksonObjectMapper()
    private val key: SecretKey
    private val credentials: MutableMap<String, Credential> = mutableMapOf()

    init {
        ensureDirectories()
        key = loadOrCreateKey()
        loadCredentials()
    }

    private fun ensureDirectories() {
        if (!credDir.toFile().exists()) {
            credDir.toFile().mkdirs()
        }
    }

    private fun loadOrCreateKey(): SecretKey {
        if (keyFile.exists()) {
            val keyBytes = Files.readAllBytes(keyFile.toPath())
            return SecretKeySpec(keyBytes, "AES")
        } else {
            val newKey = EncryptionUtils.generateKey()
            val keyBytes = newKey.encoded
            Files.write(keyFile.toPath(), keyBytes)
            keyFile.setReadable(false, false)
            keyFile.setReadable(true, true)
            keyFile.setWritable(false, false)
            keyFile.setWritable(true, true)
            return newKey
        }
    }

    private fun loadCredentials() {
        if (credFile.exists()) {
            val encryptedData = Files.readAllBytes(credFile.toPath())
            val jsonData = EncryptionUtils.decrypt(encryptedData, key)
            val creds: List<Credential> = objectMapper.readValue(jsonData)
            for (cred in creds) {
                credentials[cred.id] = cred
            }
        }
    }

    private fun saveCredentials() {
        val credsList = credentials.values.toList()
        val jsonData = objectMapper.writeValueAsBytes(credsList)
        val encryptedData = EncryptionUtils.encrypt(jsonData, key)
        Files.write(credFile.toPath(), encryptedData)
        credFile.setReadable(false, false)
        credFile.setReadable(true, true)
        credFile.setWritable(false, false)
        credFile.setWritable(true, true)
    }

     fun addCredential(credential: Credential) {
        credentials[credential.id] = credential
        saveCredentials()
    }

     fun getCredential(id: String): Credential? {
        return credentials[id]
    }

     fun updateCredential(credential: Credential) {
        credentials[credential.id] = credential
        saveCredentials()
    }

     fun deleteCredential(id: String) {
        credentials.remove(id)
        saveCredentials()
    }

     fun listCredentials(): List<Credential> {
        return credentials.values.toList()
    }

     fun deleteAllCredentials() {
        credentials.clear()
        credFile.delete()
    }
} 
