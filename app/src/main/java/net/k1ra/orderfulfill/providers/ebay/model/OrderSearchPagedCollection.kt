package net.k1ra.orderfulfill.providers.ebay.model

data class OrderSearchPagedCollection(
    val href: String,
    val limit: String,
    val next: String,
    val offset: Int,
    val orders: List<Order>
)