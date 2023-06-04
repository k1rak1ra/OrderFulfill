package net.k1ra.orderfulfill.feature.label_printing.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import net.k1ra.orderfulfill.feature.label_printing.db.PrinterDao
import net.k1ra.orderfulfill.feature.label_printing.db.PrinterDatabase
import net.k1ra.orderfulfill.utils.Constants

@Entity
data class Printer(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "ip") val ip: String,
    @ColumnInfo(name = "name") val name: String,
) {
    companion object {
        private var instance: PrinterDao? = null

        fun getDb(context: Context) : PrinterDao {
            if (instance == null)
                instance = Room.databaseBuilder(context, PrinterDatabase::class.java, Constants.printersDbName).build().Dao()

            return  instance!!
        }
    }
}