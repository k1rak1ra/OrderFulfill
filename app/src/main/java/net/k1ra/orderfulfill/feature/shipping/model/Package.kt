package net.k1ra.orderfulfill.feature.shipping.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import net.k1ra.orderfulfill.feature.shipping.db.PackageDao
import net.k1ra.orderfulfill.feature.shipping.db.PackageDatabase
import net.k1ra.orderfulfill.utils.Constants

@Entity
data class Package(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "length") val length: Float,
    @ColumnInfo(name = "width") val width: Float,
    @ColumnInfo(name = "height") val height: Float,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "name") val name: String,
) {
    companion object {
        private var instance: PackageDao? = null

        fun getDb(context: Context) : PackageDao {
            if (instance == null)
                instance = Room.databaseBuilder(context, PackageDatabase::class.java, Constants.packagesDbName).build().Dao()

            return  instance!!
        }
    }
}