package net.k1ra.orderfulfill.feature.shipping.model

import net.gcardone.junidecode.Junidecode.*


data class DestinationAddress(
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val countryCode: String,
    val postalCode: String,
    val stateOrProvince: String,
    val name: String,
    val phoneNumber: String
) {
    override fun toString(): String {
        return unidecode("$name\n$addressLine1${
            if (addressLine2.isNotEmpty())
                " $addressLine2"
            else
            ""
        }\n$city $stateOrProvince\n$postalCode\n$countryCode")
    }
}