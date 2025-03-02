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
            winCred.setCredential(testTarget, "testUser", "testPassword")  
            winCred.deleteCredential(testTarget)  
            true  
        } catch (e: Exception) {  
            false  
        }  
    }  
  
    override fun addCredential(prefix: String, credential: Credential) {  
        val target = "$prefix/${credential.id}"  
        winCred.setCredential(target, credential.id, credential.content)  
    }  
  
    override fun getCredential(prefix: String, id: String): Credential? {  
        val target = "$prefix/$id"  
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
        val target = "$prefix/$id"  
        try {  
            winCred.deleteCredential(target)  
        } catch (e: Exception) {  
            // Handle exceptions or log if necessary  
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