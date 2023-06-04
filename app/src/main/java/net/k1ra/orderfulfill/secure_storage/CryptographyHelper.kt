package net.k1ra.orderfulfill.secure_storage

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class CryptographyHelper {
    companion object {
        /**
         * Method to encrypt/decrypt data
         *
         * @param input - the input data
         * @param iv - the IV
         * @param cipherMode - ENCRYPT_MODE or DECRYPT_MODE
         */
        fun runAES(input: ByteArray, iv: ByteArray, cipherMode: Int): ByteArray {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(cipherMode, EncryptionKeyManager.getKey(), GCMParameterSpec(128, iv))
            return cipher.doFinal(input)
        }

        /**
         * Generates SecureRandom 16-byte IV
         */
        fun genIV() : ByteArray {
            val r = SecureRandom()
            val ivBytes = ByteArray(12)
            r.nextBytes(ivBytes)

            return ivBytes
        }
    }
}