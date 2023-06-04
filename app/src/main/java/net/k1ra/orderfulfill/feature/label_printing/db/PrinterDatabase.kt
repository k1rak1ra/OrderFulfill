package net.k1ra.orderfulfill.feature.label_printing.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.k1ra.orderfulfill.feature.label_printing.model.Printer

@Database(entities = [Printer::class], version = 1)
abstract class PrinterDatabase : RoomDatabase() {
    abstract fun Dao(): PrinterDao
}