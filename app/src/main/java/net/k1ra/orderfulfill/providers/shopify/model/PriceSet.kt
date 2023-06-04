package net.k1ra.orderfulfill.providers.shopify.model

import com.google.gson.annotations.SerializedName

data class PriceSet(
    @SerializedName("shop_money")
    val shopMoney: Amount,
    @SerializedName("presentment_money")
    val presentmentMoney: Amount
)