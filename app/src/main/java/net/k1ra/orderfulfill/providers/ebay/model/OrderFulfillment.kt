package net.k1ra.orderfulfill.providers.ebay.model

open class OrderFulfillment(
    val lineItems: List<FulfilledLineItem>
)