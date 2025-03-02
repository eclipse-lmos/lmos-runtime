package org.eclipse.lmos.cli.credential.manager

import com.sun.jna.LastErrorException
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinBase.FILETIME
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

/**

Provides access to the Advapi32 credential functions via JNA.
*/
interface CredAdvapi32 : StdCallLibrary {

/**

Represents the CREDENTIAL_ATTRIBUTE structure.

See:

https://msdn.microsoft.com/en-us/library/windows/desktop/aa374790(v=vs.85).aspx
*/
open class CREDENTIAL_ATTRIBUTE : Structure() {

/**

Used when passing a pointer to a CREDENTIAL_ATTRIBUTE structure.
*/
class ByReference : CREDENTIAL_ATTRIBUTE(), Structure.ByReference
override fun getFieldOrder() = listOf("Keyword", "Flags", "ValueSize", "Value")

/**

Name of the application-specific attribute (should be of the form CompanyName_Name).
Cannot be longer than CRED_MAX_STRING_LENGTH (256) characters.
*/
var Keyword: String? = null
/**

Reserved flags; must be set to 0.
*/
var Flags: Int = 0
/**

Length in bytes of the data pointed to by [Value].
Cannot be larger than CRED_MAX_VALUE_SIZE (256).
*/
var ValueSize: Int = 0
/**

Pointer to the attribute data.
If this is a text string, it must be in UNICODE and should not include the trailing zero.
*/
var Value: Pointer? = null
}
/**

A helper class that wraps a pointer to a CREDENTIAL_ATTRIBUTE structure.
*/
class PCREDENTIAL_ATTRIBUTE : Structure {
override fun getFieldOrder() = listOf("credential_attribute")

constructor() : super()

/**

Constructs a PCREDENTIAL_ATTRIBUTE from a byte array.
*/
constructor(data: ByteArray) : super(Memory(data.size.toLong())) {
pointer.write(0, data, 0, data.size)
read()
}
/**

Constructs a PCREDENTIAL_ATTRIBUTE from a Pointer.
*/
constructor(memory: Pointer?) : super(memory) {
read()
}
var credential_attribute: Pointer? = null
}

/**

Represents the CREDENTIAL structure.

See:

https://msdn.microsoft.com/en-us/library/windows/desktop/aa374788(v=vs.85).aspx
*/
class CREDENTIAL : Structure {
override fun getFieldOrder() = listOf(
"Flags",
"Type",
"TargetName",
"Comment",
"LastWritten",
"CredentialBlobSize",
"CredentialBlob",
"Persist",
"AttributeCount",
"Attributes",
"TargetAlias",
"UserName"
)

constructor() : super()

/**

Constructs a CREDENTIAL structure backed by [Memory] of the given size.
*/
constructor(size: Int) : super(Memory(size.toLong()))
/**

Constructs a CREDENTIAL structure from an existing [Pointer].
*/
constructor(memory: Pointer?) : super(memory) {
read()
}
/**

Characteristics flags of the credential.
*/
var Flags: Int = 0
/**

Credential type. Must be one of the CRED_TYPE_* constants.
*/
var Type: Int = 0
/**

The target name of the credential.
*/
var TargetName: String? = null
/**

A comment associated with the credential.
*/
var Comment: String? = null
/**

The time the credential was last written (in UTC).
*/
var LastWritten: FILETIME? = null
/**

Size in bytes of the credential blob.
*/
var CredentialBlobSize: Int = 0
/**

Pointer to the credential secret data.
*/
var CredentialBlob: Pointer? = null
/**

Persistence type for the credential.
*/
var Persist: Int = 0
/**

The number of application-defined attributes.
*/
var AttributeCount: Int = 0
/**

Application-defined attributes.
Note: Currently a single attribute reference; an array may be required for multiple attributes.
*/
var Attributes: CREDENTIAL_ATTRIBUTE.ByReference? = null
/**

An alias for the TargetName member.
*/
var TargetAlias: String? = null
/**

The user name associated with the credential.
*/
var UserName: String? = null
}
/**

A helper class that wraps a pointer to a CREDENTIAL structure.
*/
class PCREDENTIAL : Structure {
override fun getFieldOrder() = listOf("credential")

constructor() : super()

/**

Constructs a PCREDENTIAL from a byte array.
*/
constructor(data: ByteArray) : super(Memory(data.size.toLong())) {
pointer.write(0, data, 0, data.size)
read()
}
/**

Constructs a PCREDENTIAL from a Pointer.
*/
constructor(memory: Pointer?) : super(memory) {
read()
}
var credential: Pointer? = null
}

/**

Reads a credential from the user's credential set.
@param targetName The name of the credential.
@param type The type of the credential (one of the CRED_TYPE_* values).
@param flags Reserved; must be 0.
@param pcredential Output parameter that receives the pointer to the credential.
@return True if the call succeeded; false otherwise.
@throws LastErrorException if an error occurs (use [com.sun.jna.Native.getLastError] to retrieve the error).
*/
@Throws(LastErrorException::class)
fun CredRead(targetName: String?, type: Int, flags: Int, pcredential: PCREDENTIAL?): Boolean
/**

Creates or updates a credential in the user's credential set.
@param credential The credential information to write.
@param flags Flags that control the function's operation, such as [CRED_PRESERVE_CREDENTIAL_BLOB].
@return True if the credential was written successfully; false otherwise.
@throws LastErrorException if an error occurs.
*/
@Throws(LastErrorException::class)
fun CredWrite(credential: CREDENTIAL?, flags: Int): Boolean
/**

Deletes a credential from the user's credential set.
@param targetName The name of the credential to delete.
@param type The type of the credential (one of the CRED_TYPE_* values).
@param flags Reserved; must be 0.
@return True if the credential was deleted successfully; false otherwise.
@throws LastErrorException if an error occurs.
*/
@Throws(LastErrorException::class)
fun CredDelete(targetName: String?, type: Int, flags: Int): Boolean
/**

Frees memory allocated for a credential.
@param credential Pointer to the credential memory to free.
@throws LastErrorException if an error occurs.
*/
@Throws(LastErrorException::class)
fun CredFree(credential: Pointer?)
companion object {
/**
* Loads the native Advapi32 library and exposes the credential functions.
*/
val INSTANCE: CredAdvapi32 = Native.load(
"Advapi32",
CredAdvapi32::class.java,
W32APIOptions.UNICODE_OPTIONS
)


 // CredRead flags.  
 const val CRED_FLAGS_PROMPT_NOW: Int = 0x0002  
 const val CRED_FLAGS_USERNAME_TARGET: Int = 0x0004  

 // Credential types.  
 const val CRED_TYPE_GENERIC: Int = 1  
 const val CRED_TYPE_DOMAIN_PASSWORD: Int = 2  
 const val CRED_TYPE_DOMAIN_CERTIFICATE: Int = 3  
 const val CRED_TYPE_DOMAIN_VISIBLE_PASSWORD: Int = 4  
 const val CRED_TYPE_GENERIC_CERTIFICATE: Int = 5  
 const val CRED_TYPE_DOMAIN_EXTENDED: Int = 6  
 const val CRED_TYPE_MAXIMUM: Int = 7  // Maximum supported credential type.  
 const val CRED_TYPE_MAXIMUM_EX: Int = CRED_TYPE_MAXIMUM + 1000

 // CredWrite flag.  
 const val CRED_PRESERVE_CREDENTIAL_BLOB: Int = 0x1  

 // Credential persistence values.  
 const val CRED_PERSIST_NONE: Int = 0  
 const val CRED_PERSIST_SESSION: Int = 1  
 const val CRED_PERSIST_LOCAL_MACHINE: Int = 2  
 const val CRED_PERSIST_ENTERPRISE: Int = 3  
}
}