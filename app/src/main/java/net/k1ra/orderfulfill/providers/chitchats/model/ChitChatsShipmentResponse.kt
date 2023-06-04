package net.k1ra.orderfulfill.providers.chitchats.model

import com.google.gson.annotations.SerializedName

data class ChitChatsShipmentResponse(
    val id: String,
    val rates: List<Rate>,
    @SerializedName("postage_label_zpl_url")
    val postageLabelZplUrl: String,
    val status: String
)