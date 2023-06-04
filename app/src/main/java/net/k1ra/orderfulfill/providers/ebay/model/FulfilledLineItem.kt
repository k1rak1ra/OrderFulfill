package net.k1ra.orderfulfill.providers.ebay.model

data class FulfilledLineItem(
    val lineItemId: String,
    val quantity: Int
)