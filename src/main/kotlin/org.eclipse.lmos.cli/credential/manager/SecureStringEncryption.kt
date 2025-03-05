package org.eclipse.lmos.cli.credential.manager

import org.slf4j.LoggerFactory
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Securely encrypts a string using AES-GCM and stores it in a file
 * Uses PBKDF2 for key derivation with a random salt
 */
class SecureStringEncryption {
    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_ALGORITHM = "AES"
        private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 65536 // High iteration count for security
        private const val KEY_LENGTH = 256
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
        private const val SALT_LENGTH = 32
    }

    private val log = LoggerFactory.getLogger(SecureStringEncryption::class.java)

    /**
     * Encrypts a string with a password and writes it to a file
     * @param plainText The string to encrypt
     * @param password The password used for encryption
     * @param outputFile The file to write the encrypted data to
     */
    fun encrypt(plainText: String, password: String): ByteArray {
        // Generate random salt for PBKDF2
        try {
            val salt = ByteArray(SALT_LENGTH).apply {
                SecureRandom().nextBytes(this)
            }

            // Generate a secure key using PBKDF2
            val secretKey = deriveKey(password, salt)

            // Generate random IV for GCM mode
            val iv = ByteArray(GCM_IV_LENGTH).apply {
                SecureRandom().nextBytes(this)
            }

            // Initialize cipher for encryption
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)

            // Encrypt the data
            val encryptedBytes = cipher.doFinal(plainText.toByteArray())

            // Combine salt, IV, and encrypted data for storage
            val outputData = ByteArray(salt.size + iv.size + encryptedBytes.size).apply {
                System.arraycopy(salt, 0, this, 0, salt.size)
                System.arraycopy(iv, 0, this, salt.size, iv.size)
                System.arraycopy(encryptedBytes, 0, this, salt.size + iv.size, encryptedBytes.size)
            }
            return outputData;
        } catch (e: Exception) {
            log.error("Error encrypting data: {}", e.message)
            throw e
        }
    }

    /**
     * Decrypts a string from a file using the provided password
     * @param password The password used for decryption
     * @param inputFile The file containing the encrypted data
     * @return The decrypted string
     */
    fun readAndDecrypt(password: String, encryptedData: ByteArray): String {

        try {
        val salt = encryptedData.copyOfRange(0, SALT_LENGTH)
        val iv = encryptedData.copyOfRange(SALT_LENGTH, SALT_LENGTH + GCM_IV_LENGTH)
        val encrypted = encryptedData.copyOfRange(SALT_LENGTH + GCM_IV_LENGTH, encryptedData.size)

        // Derive the key using the same password and extracted salt
        val secretKey = deriveKey(password, salt)

        // Initialize cipher for decryption
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

        // Decrypt the data
        val decryptedBytes = cipher.doFinal(encrypted)

            log.info("Data decrypted successfully")
            return String(decryptedBytes)
        } catch (e: Exception) {
            log.error("Error decrypting data: {}", e.message)
            throw e
        }
    }

    /**
     * Derives an encryption key from a password and salt using PBKDF2
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }
}