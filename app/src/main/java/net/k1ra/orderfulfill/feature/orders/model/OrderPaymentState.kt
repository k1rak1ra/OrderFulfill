package net.k1ra.orderfulfill.feature.orders.model

import android.content.Context
import android.content.res.ColorStateList
import net.k1ra.orderfulfill.R

enum class OrderPaymentState {
    PAID, FAILED, FULLY_REFUNDED, PARTIALLY_REFUNDED, PENDING;

    override fun toString(): String {
        return when(this) {
            PAID -> "Paid"
            FAILED -> "Failed"
            FULLY_REFUNDED -> "Refunded"
            PARTIALLY_REFUNDED -> "Partially Refunded"
            PENDING -> "Pending"
        }
    }

    fun getColor(context: Context) : ColorStateList {
        return ColorStateList.valueOf(context.getColor(
            when(this) {
                PAID -> R.color.chip_green_color
                FAILED -> R.color.chip_red_color
                FULLY_REFUNDED -> R.color.chip_orange_color
                PARTIALLY_REFUNDED -> R.color.chip_orange_color
                PENDING -> R.color.chip_yellow_color
            }
        ))
    }
}