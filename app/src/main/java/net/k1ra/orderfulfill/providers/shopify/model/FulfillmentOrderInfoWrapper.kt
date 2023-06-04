package net.k1ra.orderfulfill.providers.shopify.model

import com.google.gson.annotations.SerializedName

data class FulfillmentOrderInfoWrapper(
    @SerializedName("fulfillment_orders")
    val fulfillmentOrders: List<FulfillmentOrderInfo>
)