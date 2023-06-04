package net.k1ra.orderfulfill.providers.ebay.model

class OrderFulfillmentWithTracking(
    lineItems: List<FulfilledLineItem>,
    val shippingCarrierCode: String,
    val trackingNumber: String,
) : OrderFulfillment(lineItems)