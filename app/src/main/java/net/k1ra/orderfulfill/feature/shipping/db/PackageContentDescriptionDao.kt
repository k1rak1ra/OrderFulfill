package net.k1ra.orderfulfill.feature.shipping.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.k1ra.orderfulfill.feature.shipping.model.PackageContentDescription

@Dao
interface PackageContentDescriptionDao {
    @Query("SELECT * FROM packagecontentdescription WHERE platform = :platform")
    fun getById(platform: Int): PackageContentDescription?

    @Insert
    fun insert(vararg pck: PackageContentDescription)

    @Query("DELETE FROM packagecontentdescription WHERE platform = :platform")
    fun delete(platform: Int)
}