package net.k1ra.orderfulfill.providers.ebay.model

data class ShippingStep(
    val shippingCarrierCode: String,
    val shippingServiceCode: String,
    val shipTo: ExtendedContact,
    val shipToReferenceId: String
)