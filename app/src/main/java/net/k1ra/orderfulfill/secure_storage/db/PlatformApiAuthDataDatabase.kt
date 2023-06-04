package net.k1ra.orderfulfill.secure_storage.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlatformApiAuthData::class], version = 1)
abstract class PlatformApiAuthDataDatabase : RoomDatabase() {
    abstract fun PlatformApiAuthDataDao(): PlatformApiAuthDataDao
}