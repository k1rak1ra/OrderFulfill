package net.k1ra.orderfulfill.feature.shipping.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.k1ra.orderfulfill.feature.shipping.model.PackageContentDescription

@Database(entities = [PackageContentDescription::class], version = 1)
abstract class PackageContentDescriptionDatabase : RoomDatabase() {
    abstract fun Dao(): PackageContentDescriptionDao
}