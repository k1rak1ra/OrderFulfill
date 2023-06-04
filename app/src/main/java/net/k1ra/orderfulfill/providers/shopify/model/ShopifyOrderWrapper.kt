package net.k1ra.orderfulfill.providers.shopify.model

data class ShopifyOrderWrapper(
    val orders: List<ShopifyOrder>
)