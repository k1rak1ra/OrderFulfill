package net.k1ra.orderfulfill.providers.shopify.model

import com.google.gson.annotations.SerializedName

data class Address(
    val name: String,
    val address1: String,
    val address2: String?,
    val city: String,
    val phone: String?,
    val zip: String,
    @SerializedName("province_code")
    val provinceCode: String?,
    @SerializedName("country_code")
    val countryCode: String
)