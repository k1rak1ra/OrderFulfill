package net.k1ra.orderfulfill.providers.shopify.model

import com.google.gson.annotations.SerializedName

open class ShopifyFulfillmentWithTracking(
    notifyCustomer: Boolean,
    lineItemsByFulfillmentOrder: List<FulfillmentOrderWithLineItems>,
    @SerializedName("tracking_info")
    val trackingInfo: ShopifyTrackingInfo
) : ShopifyFulfillment(notifyCustomer, lineItemsByFulfillmentOrder)