package net.k1ra.orderfulfill.providers.shopify.model

import com.google.gson.annotations.SerializedName

data class ShopifyOrder(
    val id: String,
    @SerializedName("total_shipping_price_set")
    val totalShippingPriceSet: PriceSet,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("contact_email")
    val contactEmail: String,
    @SerializedName("shipping_address")
    val shippingAddress: Address,
    @SerializedName("line_items")
    val lineItems: List<LineItem>
)