package net.k1ra.orderfulfill.feature.label_printing.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.k1ra.orderfulfill.feature.label_printing.model.Printer

@Dao
interface PrinterDao {
    @Query("SELECT * FROM printer")
    fun getAll(): List<Printer>

    @Insert
    fun insert(vararg printer: Printer)

    @Query("DELETE FROM printer WHERE id = :id")
    fun delete(id: Int)
}