package net.k1ra.orderfulfill.feature.shipping.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import net.k1ra.orderfulfill.feature.shipping.db.PackageContentDescriptionDao
import net.k1ra.orderfulfill.feature.shipping.db.PackageContentDescriptionDatabase
import net.k1ra.orderfulfill.utils.Constants

@Entity
data class PackageContentDescription(
    @PrimaryKey val platform: Int,
    @ColumnInfo(name = "description") val description: String,
) {
    companion object {
        private var instance: PackageContentDescriptionDao? = null

        fun getDb(context: Context) : PackageContentDescriptionDao {
            if (instance == null)
                instance = Room.databaseBuilder(context, PackageContentDescriptionDatabase::class.java, Constants.packageDescriptionsDbName).build().Dao()

            return  instance!!
        }
    }
}