package org.eclipse.lmos.cli.llm

import net.mamoe.yamlkt.Yaml
import org.eclipse.lmos.cli.constants.LmosCliConstants.PREFIX
import org.eclipse.lmos.cli.credential.Credential
import org.eclipse.lmos.cli.factory.CredentialManagerFactory

interface LLMConfigManager {

    fun addLLMConfig(llmConfig: LLMConfig): LLMConfig
    fun getLLMConfig(id: String): LLMConfig?
    fun updateLLMConfig(llmConfig: LLMConfig): LLMConfig
    fun deleteLLMConfig(id: String)
    fun listLLMConfig(): Set<String>
    fun deleteAllLLMConfig(): Set<String>

}

class DefaultLLMConfigManager : LLMConfigManager {


    override fun addLLMConfig(llmConfig: LLMConfig): LLMConfig {
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        val configYaml = Yaml().encodeToString(LLMConfig.serializer(), llmConfig)
        val credential = Credential(llmConfig.id, configYaml)
        credentialManager.addCredential(PREFIX, credential)
        return llmConfig
    }

    override fun getLLMConfig(id: String): LLMConfig? {
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        val credential = credentialManager.getCredential(PREFIX, id)
        return if (credential == null) {
            null
        } else {
            Yaml.decodeFromString(LLMConfig.serializer(), credential.content)
        }
    }

    override fun updateLLMConfig(llmConfig: LLMConfig): LLMConfig {
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        credentialManager.deleteCredential(PREFIX, llmConfig.id)
        credentialManager.addCredential(PREFIX, Credential(llmConfig.id, Yaml().encodeToString(LLMConfig.serializer(), llmConfig)))
        return llmConfig
    }

    override fun deleteLLMConfig(id: String) {
        val credentialManager = CredentialManagerFactory().getCredentialManager()
            credentialManager.deleteCredential(PREFIX, id)
        }

    override fun listLLMConfig(): Set<String> {
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        val listCredentials = credentialManager.listCredentials(PREFIX)
        return listCredentials.map { it.id }.toSet()
    }

    override fun deleteAllLLMConfig(): Set<String> {
        val list: Set<String> = setOf()
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        credentialManager.listCredentials(PREFIX).forEach {
            credentialManager.deleteCredential(PREFIX, it.id)
            list.plus(it)
        }
        return list
    }

}

