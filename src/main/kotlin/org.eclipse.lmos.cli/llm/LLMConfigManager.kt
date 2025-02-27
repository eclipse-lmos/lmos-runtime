package org.eclipse.lmos.cli.llm

import net.mamoe.yamlkt.Yaml
import org.eclipse.lmos.cli.constants.LmosCliConstants.PREFIX
import org.eclipse.lmos.cli.credential.Credential
import org.eclipse.lmos.cli.factory.CredentialManagerFactory

interface LLMConfigManager {

    fun addLLMConfig(llmConfig: LLMConfig): LLMConfig?
    fun getLLMConfig(id: String): LLMConfig?
    fun updateLLMConfig(llmConfig: LLMConfig): LLMConfig?
    fun deleteLLMConfig(id: String): LLMConfig?
    fun listLLMConfig(): Set<LLMConfig>
    fun deleteAllLLMConfig(): Set<LLMConfig>

}

class DefaultLLMConfigManager : LLMConfigManager {


    override fun addLLMConfig(llmConfig: LLMConfig): LLMConfig? {
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        credentialManager.getCredential(PREFIX, llmConfig.id)?.let {
            return null
        }

        val configYaml = Yaml().encodeToString(llmConfig)
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

    override fun updateLLMConfig(llmConfig: LLMConfig): LLMConfig? {
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        credentialManager.deleteCredential(PREFIX, llmConfig.id)
        credentialManager.addCredential(PREFIX, Credential(llmConfig.id, Yaml().encodeToString(llmConfig)))
        return llmConfig
    }

    override fun deleteLLMConfig(id: String): LLMConfig? {
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        val credential = credentialManager.getCredential(PREFIX, id)
        if (credential == null) {
            return null
        } else {
            credentialManager.deleteCredential(PREFIX, id)
            return Yaml().decodeFromString(LLMConfig.serializer(), credential.content)
        }
    }

    override fun listLLMConfig(): Set<LLMConfig> {
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        return credentialManager.listCredentials(PREFIX).map {
            LLMConfig(id = it.id, "","","","")
        }.toSet()
    }

    override fun deleteAllLLMConfig(): Set<LLMConfig> {
        val list: Set<LLMConfig> = setOf()
        val credentialManager = CredentialManagerFactory().getCredentialManager()
        credentialManager.listCredentials(PREFIX).forEach {
            credentialManager.deleteCredential(PREFIX, it.id)
            list.plus(Yaml().decodeAnyFromString(it.content) as LLMConfig)
        }
        return list
    }

}

