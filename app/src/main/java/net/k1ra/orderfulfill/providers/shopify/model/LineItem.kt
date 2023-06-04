package net.k1ra.orderfulfill.providers.shopify.model

import com.google.gson.annotations.SerializedName

data class LineItem(
    val id: String,
    @SerializedName("price_set")
    val priceSet: PriceSet,
    val name: String,
    @SerializedName("variant_title")
    val variantTitle: String?,
    val quantity: Int,
    @SerializedName("product_id")
    val productId: String
)