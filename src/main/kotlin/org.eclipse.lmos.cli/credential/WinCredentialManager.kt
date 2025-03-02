package org.eclipse.lmos.cli.credential

import org.eclipse.lmos.cli.credential.manager.CredentialManager

class WinCredentialManager : CredentialManager {
  
    private val winCred = WinCred()  
  
    override fun credentialManagerType(): CredentialManagerType {  
        return CredentialManagerType.WIN  
    }  
  
    override fun testCredentialManager(): Boolean {  
        return try {  
            val testTarget = "CredentialManagerTestTarget"
            val user = "testUser4"
            //winCred.deleteCredential2(testTarget, user)
            winCred.setCredential(testTarget, user, "testPassword")
            winCred.listCredentials("")
            winCred.deleteCredential(testTarget, user)
            true  
        } catch (e: Exception) {  
            false  
        }  
    }  
  
    override fun addCredential(prefix: String, credential: Credential) {
        winCred.setCredential("$prefix:", credential.id, credential.content)
    }  
  
    override fun getCredential(prefix: String, id: String): Credential? {  
        val target = "$prefix:$id"
        return try {  
            val winCredential = winCred.getCredential(target)  
            Credential(  
                id = winCredential.username ?: id,  
                content = winCredential.password  
            )  
        } catch (e: Exception) {  
            null  
        }  
    }  
  
    override fun updateCredential(prefix: String, credential: Credential) {  
        addCredential(prefix, credential)  
    }  
  
    override fun deleteCredential(prefix: String, id: String) {
        try {  
            winCred.deleteCredential(prefix, id)
        } catch (e: Exception) {
            println(e)
        }  
    }  
  
    override fun listCredentials(prefix: String): Set<Credential> {  
        return try {  
            winCred.listCredentials(prefix).map {  
                Credential(  
                    id = it.username ?: "",  
                    content = it.password  
                )  
            }.toSet()  
        } catch (e: Exception) {  
            emptySet()  
        }  
    }  
  
    override fun deleteAllCredentials(prefix: String) {  
        val credentials = listCredentials(prefix)  
        for (credential in credentials) {  
            deleteCredential(prefix, credential.id)  
        }  
    }  
}  