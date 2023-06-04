package net.k1ra.orderfulfill.providers.ebay.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String
)