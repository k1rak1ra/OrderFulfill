package net.k1ra.orderfulfill.providers.ebay.model

import com.google.gson.annotations.SerializedName

data class TokenData(
    @SerializedName("access_token")
    var accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)