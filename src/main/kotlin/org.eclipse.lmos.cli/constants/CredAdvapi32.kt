package org.eclipse.lmos.cli.constants

import com.sun.jna.*  
import com.sun.jna.platform.win32.WinBase
import com.sun.jna.ptr.*  
import com.sun.jna.win32.StdCallLibrary  
import com.sun.jna.win32.W32APIOptions  

interface CredAdvapi32 : StdCallLibrary {    
    companion object {
        var INSTANCE: CredAdvapi32 = Native.load("Advapi32", CredAdvapi32::class.java, W32APIOptions.UNICODE_OPTIONS)  
        const val CRED_TYPE_GENERIC = 1    
        const val CRED_PERSIST_ENTERPRISE = 3    
    }    

    @Throws(LastErrorException::class)  
    fun CredRead(targetName: String, type: Int, flags: Int, pcredential: PCREDENTIAL): Boolean  

    @Throws(LastErrorException::class)  
    fun CredWrite(credential: CREDENTIAL, flags: Int): Boolean  

    @Throws(LastErrorException::class)  
    fun CredDelete(targetName: String, type: Int, flags: Int): Boolean  

    @Throws(LastErrorException::class)  
    fun CredFree(buffer: Pointer)  

    @Throws(LastErrorException::class)  
    fun CredEnumerate(filter: String?, flags: Int, count: IntByReference, pCredentials: PointerByReference?): Boolean

    class PCREDENTIAL : Structure() {    
        @JvmField    
        var credential: Pointer? = null    

        override fun getFieldOrder() = listOf("credential")    
    }    

    class CREDENTIAL() : Structure() {
        @JvmField
        var Flags: Int = 0
        @JvmField
        var Type: Int = 0
        @JvmField
        var TargetName: WString? = null
        @JvmField
        var Comment: WString? = null
        @JvmField
        var LastWritten: WinBase.FILETIME = WinBase.FILETIME()
        @JvmField
        var CredentialBlobSize: Int = 0
        @JvmField
        var CredentialBlob: Pointer? = null
        @JvmField
        var Persist: Int = 0
        @JvmField
        var AttributeCount: Int = 0
        @JvmField
        var Attributes: Pointer? = null
        @JvmField
        var TargetAlias: String? = null
        @JvmField
        var UserName: WString? = null

        constructor(pointer: Pointer?) : this() {    
            useMemory(pointer)    
            read()    
        }    

        override fun getFieldOrder() = listOf(    
            "Flags", "Type", "TargetName", "Comment", "LastWritten",    
            "CredentialBlobSize", "CredentialBlob", "Persist", "AttributeCount",    
            "Attributes", "TargetAlias", "UserName"    
        )    
    }    
}    