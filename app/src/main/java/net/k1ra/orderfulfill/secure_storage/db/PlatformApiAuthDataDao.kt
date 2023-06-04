package net.k1ra.orderfulfill.secure_storage.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlatformApiAuthDataDao {
    @Query("SELECT * FROM platformapiauthdata WHERE platform = :platform")
    fun get(platform: Int): PlatformApiAuthData?

    @Query("SELECT * FROM platformapiauthdata WHERE iv = :iv")
    fun getByIv(iv: String): List<PlatformApiAuthData>

    @Insert
    fun insert(vararg platformApiAuthData: PlatformApiAuthData)

    @Query("DELETE FROM platformapiauthdata WHERE platform = :platform")
    fun delete(platform: Int)
}