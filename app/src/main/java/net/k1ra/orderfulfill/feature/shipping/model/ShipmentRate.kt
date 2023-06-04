package net.k1ra.orderfulfill.feature.shipping.model

data class ShipmentRate(
    val shipmentId: String,
    val rateId: String,
    val name: String,
    val deliveryTime: String,
    val trackingDescription: String,
    val isInsured: Boolean,
    val price: String
)