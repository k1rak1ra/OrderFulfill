package net.k1ra.orderfulfill.feature.orders.model

import net.k1ra.orderfulfill.feature.shipping.model.DestinationAddress
import net.k1ra.orderfulfill.model.RequestFailureType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.streams.toList

abstract class PendingOrders {
    class Success(
        val orders: List<PendingOrder>
    ) : PendingOrders()

    class Failure(
        val type: RequestFailureType,
        val message: String
    ) : PendingOrders()

    data class PendingOrder(
        val id: String,
        val recipient: DestinationAddress,
        val shippingCost: String,
        val paymentState: OrderPaymentState,
        val orderCreated: ZonedDateTime,
        val products: List<Product>
    ) {
        val totalCost = "%.2f".format(products.stream().map { it.price.toFloat() * it.quantity }.toList().sum())
        val totalQuantity = products.stream().map {  it.quantity }.toList().sum()
        val createdStr: String = orderCreated.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }
}