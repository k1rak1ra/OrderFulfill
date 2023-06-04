package net.k1ra.orderfulfill.providers.shopify.model

import com.google.gson.annotations.SerializedName

data class FulfillmentOrderInfo(
    val id: String,
    @SerializedName("line_items")
    val lineItems: List<LineItemForFulfillmentOrder>
)