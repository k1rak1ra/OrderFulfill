package net.k1ra.orderfulfill.providers.ebay.model

data class FulfillmentStartInstruction(
    val shippingStep: ShippingStep,
    val minEstimatedDeliveryDate: String,
    val maxEstimatedDeliveryDate: String
)