
package org.eclipse.lmos.cli.credential.manager

import org.eclipse.lmos.cli.credential.Credential
import org.eclipse.lmos.cli.credential.CredentialManagerType

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
