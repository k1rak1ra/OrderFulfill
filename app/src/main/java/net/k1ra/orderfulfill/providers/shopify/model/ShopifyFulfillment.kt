package net.k1ra.orderfulfill.providers.shopify.model

import com.google.gson.annotations.SerializedName

open class ShopifyFulfillment(
    @SerializedName("notify_customer")
    val notifyCustomer: Boolean,
    @SerializedName("line_items_by_fulfillment_order")
    val lineItemsByFulfillmentOrder: List<FulfillmentOrderWithLineItems>
)