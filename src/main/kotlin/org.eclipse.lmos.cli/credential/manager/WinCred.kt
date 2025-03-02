package org.eclipse.lmos.cli.credential.manager

import com.sun.jna.*
import java.io.UnsupportedEncodingException


/**
 * This class exposes functions from credential manager on Windows platform
 * via JNA.
 *
 * Please refer to MSDN documentations for each method usage pattern
 */

fun main() {
    val wc = WinCred()
    // Create a credential
    wc.setCredential("Application Name", "Username", "Password")
    // Get a credential
    val cred = wc.getCredential("Application Name");
    val username = cred.username
    val password = cred.password

    println(username);
    println(password);
    // Delete a credential
    wc.deleteCredential("Application Name")
}

class WinCred : CredAdvapi32 {

    // Converted the inner Credential class into a data class.
// A secondary constructor is provided to handle nullable input values,
// using the Elvis operator to default nulls to empty strings.
    data class Credential(val target: String?, val username: String?, val password: String)

    // Retrieves a credential for the specified target from Windows Credential Manager.
    fun getCredential(target: String): Credential {
        val pcredMem = CredAdvapi32.PCREDENTIAL()


        try {
            // Use the generic type constant rather than a hardcoded literal.
            if (CredRead(target, CredAdvapi32.CRED_TYPE_GENERIC, 0, pcredMem)) {
                val credMem = CredAdvapi32.CREDENTIAL(pcredMem.credential)
                // Assume CredentialBlob is non-null (throws if it is null)
                val passwordBytes = credMem.CredentialBlob!!.getByteArray(0, credMem.CredentialBlobSize)
                // Using Charsets.UTF_16LE for clarity.
                val password = String(passwordBytes, Charsets.UTF_16LE)
                return Credential(credMem.TargetName, credMem.UserName, password)
            } else {
                throw LastErrorException(Native.getLastError())
            }
        } finally {
            CredFree(pcredMem.credential)
        }
    }

    // Sets a credential in the Windows Credential Manager.
    @Throws(UnsupportedEncodingException::class)
    fun setCredential(target: String?, userName: String?, password: String): Boolean {
        val credMem = CredAdvapi32.CREDENTIAL().apply {
            Flags = 0
            TargetName = target
            Type = CredAdvapi32.CRED_TYPE_GENERIC
            UserName = userName
            AttributeCount = 0
            Persist = CredAdvapi32.CRED_PERSIST_ENTERPRISE
            val bpassword = password.toByteArray(Charsets.UTF_16LE)
            CredentialBlobSize = bpassword.size
            CredentialBlob = getPointer(bpassword)
        }
        if (!CredWrite(credMem, 0)) {
            throw LastErrorException(Native.getLastError())
        }
        return true
    }

    // Deletes a credential from the Windows Credential Manager.
    @Throws(UnsupportedEncodingException::class)
    fun deleteCredential(target: String): Boolean {
        if (!CredDelete(target, CredAdvapi32.CRED_TYPE_GENERIC, 0)) {
            throw LastErrorException(Native.getLastError())
        }
        return true
    }

    // Overridden methods from CredAdvapi32.
// Each method is synchronized on the shared instance for thread safety.
    @Throws(LastErrorException::class)
    override fun CredRead(targetName: String?, type: Int, flags: Int, pcredential: CredAdvapi32.PCREDENTIAL?): Boolean =
        synchronized(CredAdvapi32.INSTANCE) {
            CredAdvapi32.INSTANCE.CredRead(targetName, type, flags, pcredential)
        }

    @Throws(LastErrorException::class)
    override fun CredWrite(credential: CredAdvapi32.CREDENTIAL?, flags: Int): Boolean =
        synchronized(CredAdvapi32.INSTANCE) {
            CredAdvapi32.INSTANCE.CredWrite(credential, flags)
        }

    @Throws(LastErrorException::class)
    override fun CredDelete(targetName: String?, type: Int, flags: Int): Boolean =
        synchronized(CredAdvapi32.INSTANCE) {
            CredAdvapi32.INSTANCE.CredDelete(targetName, type, flags)
        }

    @Throws(LastErrorException::class)
    override fun CredFree(credential: Pointer?) {
        synchronized(CredAdvapi32.INSTANCE) {
            CredAdvapi32.INSTANCE.CredFree(credential)
        }
    }

    companion object {
        // getPointer allocates native memory for the byte array using JNA's Memory.
        private fun getPointer(array: ByteArray): Pointer {
            return Memory(array.size.toLong()).apply {
                write(0, array, 0, array.size)
            }
        }
    }
}