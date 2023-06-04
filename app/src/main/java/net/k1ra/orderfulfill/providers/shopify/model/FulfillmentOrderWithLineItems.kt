package net.k1ra.orderfulfill.providers.shopify.model

import com.google.gson.annotations.SerializedName

data class FulfillmentOrderWithLineItems(
    @SerializedName("fulfillment_order_id")
    val fulfillmentOrderId: String,
    @SerializedName("fulfillment_order_line_items")
    val fulfillmentOrderLineItems: List<LineItemForFulfillmentOrder>
)