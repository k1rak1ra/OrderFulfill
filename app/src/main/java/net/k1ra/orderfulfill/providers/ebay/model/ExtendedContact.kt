package net.k1ra.orderfulfill.providers.ebay.model

data class ExtendedContact(
    val contactAddress: Address,
    val email: String,
    val fullName: String,
    val primaryPhone: PhoneNumber,
)