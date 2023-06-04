package net.k1ra.orderfulfill.feature.orders.model

data class Product(
    val lineItemId: String,
    val price: String,
    val name: String,
    val imageUrl: String,
    val quantity: Int,
    val otherDetails: String
)