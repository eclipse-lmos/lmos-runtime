package org.eclipse.lmos.cli.utils
  
import java.security.SecureRandom  
import javax.crypto.Cipher  
import javax.crypto.SecretKey  
import javax.crypto.spec.GCMParameterSpec  
import javax.crypto.spec.SecretKeySpec  
  
object EncryptionUtils {  
    private const val AES_KEY_SIZE_BITS = 256  
    private const val GCM_TAG_LENGTH_BITS = 128  
    private const val GCM_IV_LENGTH_BYTES = 12  
  
    fun generateKey(): SecretKey {  
        val keyBytes = ByteArray(AES_KEY_SIZE_BITS / 8)
        SecureRandom().nextBytes(keyBytes)  
        return SecretKeySpec(keyBytes, "AES")  
    }  
  
    fun encrypt(plaintext: ByteArray, key: SecretKey): ByteArray {  
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")  
        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        SecureRandom().nextBytes(iv)  
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)  
        val ciphertext = cipher.doFinal(plaintext)  
        return iv + ciphertext // Prepend IV to ciphertext  
    }  
  
    fun decrypt(ciphertext: ByteArray, key: SecretKey): ByteArray {  
        val iv = ciphertext.sliceArray(0 until GCM_IV_LENGTH_BYTES)
        val actualCiphertext = ciphertext.sliceArray(GCM_IV_LENGTH_BYTES until ciphertext.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")  
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)  
        return cipher.doFinal(actualCiphertext)  
    }  
}  