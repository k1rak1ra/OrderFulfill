package net.k1ra.orderfulfill.feature.shipping.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.k1ra.orderfulfill.feature.shipping.model.Package

@Dao
interface PackageDao {
    @Query("SELECT * FROM package")
    fun getAll(): List<Package>

    @Insert
    fun insert(vararg pck: Package)

    @Query("DELETE FROM package WHERE id = :id")
    fun delete(id: Int)
}