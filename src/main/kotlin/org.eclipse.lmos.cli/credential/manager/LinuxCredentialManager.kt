package org.eclipse.lmos.cli.credential.manager

import com.sun.jna.*
import org.eclipse.lmos.cli.credential.Credential
import org.eclipse.lmos.cli.credential.CredentialManagerType

class LinuxCred : CredentialManager {
//    data class Credential(val id: String, val content: String)

    override fun credentialManagerType(): CredentialManagerType = CredentialManagerType.LINUX

    override fun testCredentialManager(): Boolean {
        return try {
            val testTarget = "CredentialManagerTestTarget"
            val user = "testUser4"
            setCredential(testTarget, user, "testPassword")
            deleteCredential(testTarget, user)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun addCredential(prefix: String, credential: Credential) {
//        setCredential(prefix, credential.id, credential.content)
        val schema = LibSecret.INSTANCE.secret_schema_new("lmos-cli-schema", 0, "target", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, "user", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, null)
        LibSecret.INSTANCE.secret_password_store_sync(schema, LibSecret.SECRET_COLLECTION_DEFAULT, "$prefix:${credential.id}", credential.content, null, "target", prefix, "user", credential.id, null)
    }

    override fun getCredential(prefix: String, id: String): Credential? {
        return try {
            val schema = LibSecret.INSTANCE.secret_schema_new("lmos-cli-schema", 0, "target", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, "user", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, null)
            val password = LibSecret.INSTANCE.secret_password_lookup_sync(schema, null, "target", prefix, "user", id, null) ?: return null
//            val password = getSecret(prefix, id) ?: return null
            Credential(id, password)
        } catch (e: Exception) {
            null
        }
    }

    override fun updateCredential(prefix: String, credential: Credential) {
        addCredential(prefix, credential)
    }

    override fun deleteCredential(prefix: String, id: String) {
        try {
            val schema = LibSecret.INSTANCE.secret_schema_new("lmos-cli-schema", 0, "target", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, "user", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, null)
            LibSecret.INSTANCE.secret_password_clear_sync(schema, null, "target", prefix, "user", id, null)
//            deleteSecret(prefix, id)
        } catch (e: Exception) {
            println(e)
        }
    }

    override fun listCredentials(prefix: String): Set<Credential> {
        return try {
            println("Not Supported")
            listSecrets(prefix).map { Credential(it, getSecret(prefix, it) ?: "") }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    override fun deleteAllCredentials(prefix: String) {
        listCredentials(prefix).forEach { deleteCredential(prefix, it.id) }
    }

    private fun setCredential(target: String, user: String, password: String) {
        val schema = LibSecret.INSTANCE.secret_schema_new("lmos-cli-schema", 0, "target", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, "user", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, null)
        LibSecret.INSTANCE.secret_password_store_sync(schema, LibSecret.SECRET_COLLECTION_DEFAULT, "$target:$user", password, null, "target", target, "user", user, null)
    }

    private fun getSecret(target: String, user: String): String? {
        val schema = LibSecret.INSTANCE.secret_schema_new("lmos-cli-schema", 0, "target", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, "user", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, null)
        return LibSecret.INSTANCE.secret_password_lookup_sync(schema, null, "target", target, "user", user, null)
    }

    private fun deleteSecret(target: String, user: String) {
        val schema = LibSecret.INSTANCE.secret_schema_new("lmos-cli-schema", 0, "target", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, "user", LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING, null)
        LibSecret.INSTANCE.secret_password_clear_sync(schema, null, "target", target, "user", user, null)
    }

    private fun listSecrets(prefix: String): List<String> {
        return emptyList() // Listing stored secrets is not directly supported via libsecret.
    }

    private interface LibSecret : Library {
        fun secret_schema_new(name: String, flags: Int, vararg args: Any?): Pointer
        fun secret_password_store_sync(schema: Pointer, collection: String, label: String, password: String, cancellable: Pointer?, vararg args: Any?): Boolean
        fun secret_password_lookup_sync(schema: Pointer, cancellable: Pointer?, vararg args: Any?): String?
        fun secret_password_clear_sync(schema: Pointer, cancellable: Pointer?, vararg args: Any?): Boolean

        companion object {
            val INSTANCE: LibSecret = Native.load("secret-1", LibSecret::class.java)
            const val SECRET_COLLECTION_DEFAULT = "default"
            const val SECRET_SCHEMA_ATTRIBUTE_STRING = 0
        }
    }
}
