package net.k1ra.orderfulfill.secure_storage

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import net.k1ra.orderfulfill.utils.Constants
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class EncryptionKeyManager {
    companion object {
        //This is our key
        private var key: SecretKey? = null

        /**
         * If our key has been cached, returns it immediately
         * Otherwise, checks if a key has been generated and generates new one if necessary
         * Then, fetches key, caches, and returns it
         */
        @Synchronized
        fun getKey() : SecretKey {
            if (key != null)
                return key!!

            if (!doesKeyExist())
                generateNewKey()

            //Create keystore instance
            val keystore = KeyStore.getInstance("AndroidKeyStore")
            keystore.load(null)

            //Otherwise, let's fetch the existing key
            val secretKeyEntry = keystore.getEntry(Constants.apiKeyEncryptionKeyName, null) as KeyStore.SecretKeyEntry
            key = secretKeyEntry.secretKey
            return key!!
        }

        /**
         * Create keystore instance and check if key exists
         */
        @Synchronized
        private fun doesKeyExist() : Boolean {
            val keystore = KeyStore.getInstance("AndroidKeyStore")
            keystore.load(null)
            return keystore.containsAlias(Constants.apiKeyEncryptionKeyName)
        }

        /**
         * Generate a new key and store in KeyStore
         */
        @Synchronized
        private fun generateNewKey() {
            val keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                Constants.apiKeyEncryptionKeyName,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false) //We need this so we can provide our own IV
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
}
