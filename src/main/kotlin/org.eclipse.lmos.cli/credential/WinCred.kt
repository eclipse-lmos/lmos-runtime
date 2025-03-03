package org.eclipse.lmos.cli.credential

import com.sun.jna.*
import com.sun.jna.ptr.*
import org.eclipse.lmos.cli.constants.CredAdvapi32

class WinCred : CredAdvapi32 {
  
    data class Credential(val target: String, val username: String, val password: String)  
  
    fun getCredential(target: String, userName: String): Credential {
        val pcredMem = CredAdvapi32.PCREDENTIAL()
  
        try {
            if (CredRead("$target:$userName", CredAdvapi32.CRED_TYPE_GENERIC, 0, pcredMem)) {
                val credMem = CredAdvapi32.CREDENTIAL(pcredMem.credential)
                val passwordBytes = credMem.CredentialBlob?.getByteArray(0, credMem.CredentialBlobSize) ?: ByteArray(0)
                val password = String(passwordBytes, Charsets.UTF_16LE)  
                return Credential(credMem.TargetName!!.toString(), (credMem.UserName ?: "").toString(), password)
            } else {
                println("********Exception in get")
                throw LastErrorException(Native.getLastError())
            }  
        } finally {
            pcredMem.credential?.let {
                CredFree(pcredMem.credential!!)
            }
        }  
    }

    fun setCredential(target: String, userName: String, password: String): Boolean {
        val credMem = CredAdvapi32.CREDENTIAL().apply {
            Flags = 0
            TargetName = WString("$target:$userName")
            Type = CredAdvapi32.CRED_TYPE_GENERIC
            AttributeCount = 0
            Persist = CredAdvapi32.CRED_PERSIST_ENTERPRISE
            val bpassword = password.toByteArray(Charsets.UTF_16LE)
            CredentialBlobSize = bpassword.size
            CredentialBlob = getPointer(bpassword)
        }
        if (!CredWrite(credMem, 0)) {
            println("********Exception in set")
            throw LastErrorException(Native.getLastError())
        }
        return true
    }

    fun deleteCredential(target: String, userName: String): Boolean {
        val fullTargetName = "$target:$userName"  // Match the TargetName format
        if (!CredDelete(fullTargetName, CredAdvapi32.CRED_TYPE_GENERIC, 0)) {
            println("********Exception in delete")
            throw LastErrorException(Native.getLastError())
        }
        return true
    }

    fun listCredentials(prefix: String): List<Credential> {
        val count = IntByReference()  
        val pCredentials = PointerByReference()  
  
        val filter = if (prefix.isEmpty()) null else "$prefix*"  
  
        if (!CredEnumerate(filter, 0, count, pCredentials)) {  
            throw LastErrorException(Native.getLastError())  
        }  
  
        val credentials = mutableListOf<Credential>()

        val credentialPointers = pCredentials.value.getPointerArray(0, count.value)  
        for (pointer in credentialPointers) {  
            val cred = CredAdvapi32.CREDENTIAL(pointer)  
            val passwordBytes = cred.CredentialBlob?.getByteArray(0, cred.CredentialBlobSize) ?: ByteArray(0)
            val password = String(passwordBytes, Charsets.UTF_16LE)  
            credentials.add(  
                Credential(  
                    target = cred.TargetName!!.toString(),
                    username = cred.UserName.toString() ?: "",
                    password = password  
                )  
            )  
        }  
  
        CredFree(pCredentials.value)  
        return credentials  
    }  
  
    // Overridden methods from CredAdvapi32.  
    @Throws(LastErrorException::class)
    override fun CredRead(targetName: String, type: Int, flags: Int, pcredential: CredAdvapi32.PCREDENTIAL): Boolean =
        synchronized(CredAdvapi32.INSTANCE) {
            CredAdvapi32.INSTANCE.CredRead(targetName, type, flags, pcredential)
        }
  
    @Throws(LastErrorException::class)  
    override fun CredWrite(credential: CredAdvapi32.CREDENTIAL, flags: Int): Boolean =  
        synchronized(CredAdvapi32.INSTANCE) {  
            CredAdvapi32.INSTANCE.CredWrite(credential, flags)  
        }  
  
    @Throws(LastErrorException::class)  
    override fun CredDelete(targetName: String, type: Int, flags: Int): Boolean =  
        synchronized(CredAdvapi32.INSTANCE) {  
            CredAdvapi32.INSTANCE.CredDelete(targetName, type, flags)  
        }  
  
    @Throws(LastErrorException::class)  
    override fun CredFree(credential: Pointer) {  
        synchronized(CredAdvapi32.INSTANCE) {  
            CredAdvapi32.INSTANCE.CredFree(credential)  
        }  
    }  
  
    @Throws(LastErrorException::class)  
    override fun CredEnumerate(filter: String?, flags: Int, count: IntByReference, pCredentials: PointerByReference?): Boolean =
        synchronized(CredAdvapi32.INSTANCE) {  
            CredAdvapi32.INSTANCE.CredEnumerate(filter, flags, count, pCredentials)  
        }  
  
    companion object {  
        private fun getPointer(array: ByteArray): Pointer {  
            return Memory(array.size.toLong()).apply {  
                write(0, array, 0, array.size)  
            }  
        }  
    }  
}  