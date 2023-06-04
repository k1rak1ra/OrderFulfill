package net.k1ra.orderfulfill.secure_storage.db

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import net.k1ra.orderfulfill.utils.Constants
import net.k1ra.orderfulfill.model.Platform
import net.k1ra.orderfulfill.secure_storage.CryptographyHelper
import java.util.*
import javax.crypto.Cipher

@Entity
data class PlatformApiAuthData(
    @PrimaryKey val platform: Int,
    @ColumnInfo(name = "auth_data") val authData: String,
    @ColumnInfo(name = "iv") val iv: String,
) {
    companion object {
        private var dbInstance: PlatformApiAuthDataDao? = null

        /**
         * Creates DB instance if one does not exist already and returns a non-null instance
         */
        private fun initDbInstance(context: Context) : PlatformApiAuthDataDao {
            if (dbInstance == null)
                dbInstance =  Room.databaseBuilder(context, PlatformApiAuthDataDatabase::class.java, Constants.apiKeyDbName).build().PlatformApiAuthDataDao()

            return dbInstance!!
        }

        /**
         * Calls genIV until a unique (does not exist in database) IV is generated
         */
        private fun generateUniqueIv(db: PlatformApiAuthDataDao) : ByteArray {
            var iv = CryptographyHelper.genIV()

            while (db.getByIv(Base64.getEncoder().encodeToString(iv)).isNotEmpty())
                iv = CryptographyHelper.genIV()

            return iv
        }

        /**
         * Method to encrypt and store new API auth data for a platform
         */
        fun store(platform: Platform, apiAuthData: String, context: Context) {
            //Get our db instance
            val db = initDbInstance(context)

            //Delete any possible conflicting records
            db.delete(platform.ordinal)

            //Generate a unique IV
            val iv = generateUniqueIv(db)

            //Encrypt the API auth data and encode as Base64 string so we don't get any weird encoding issues
            val ciphertext = Base64.getEncoder().encodeToString(CryptographyHelper.runAES(apiAuthData.toByteArray(), iv, Cipher.ENCRYPT_MODE))

            //Build and insert the object, Base64-encode the IV so we don't get any weird encoding issues
            val platformApiAuthData = PlatformApiAuthData(
                platform.ordinal,
                ciphertext,
                Base64.getEncoder().encodeToString(iv)
            )

            db.insert(platformApiAuthData)
        }

        /**
         * Gets the stored API auth data for a platform, decrypts, and then returns it
         * Returns null if there is no stored API key for this platform
         */
        fun retrieve(platform: Platform, context: Context) : String? {
            //Get our db instance and then retrieve the record from the DB
            val db = initDbInstance(context)
            val platformApiAuthData = db.get(platform.ordinal)

            //If there's no saved data, abort and return null
            platformApiAuthData ?: return null

            //Otherwise, decrypt and return the record
            val iv = Base64.getDecoder().decode(platformApiAuthData.iv)
            return CryptographyHelper.runAES(Base64.getDecoder().decode(platformApiAuthData.authData), iv, Cipher.DECRYPT_MODE).decodeToString()
        }
    }
}