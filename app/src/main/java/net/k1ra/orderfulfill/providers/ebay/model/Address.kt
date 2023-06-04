package net.k1ra.orderfulfill.providers.ebay.model

data class Address(
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val countryCode: String,
    val postalCode: String,
    val stateOrProvince: String
)