package net.k1ra.orderfulfill.feature.shipping.model

data class ShipmentInfo(
    val trackingNumber: String?,
    val shippingLabel: String
)