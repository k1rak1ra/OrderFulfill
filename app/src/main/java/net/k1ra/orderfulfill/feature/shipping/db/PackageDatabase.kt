package net.k1ra.orderfulfill.feature.shipping.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.k1ra.orderfulfill.feature.shipping.model.Package

@Database(entities = [Package::class], version = 1)
abstract class PackageDatabase : RoomDatabase() {
    abstract fun Dao(): PackageDao
}