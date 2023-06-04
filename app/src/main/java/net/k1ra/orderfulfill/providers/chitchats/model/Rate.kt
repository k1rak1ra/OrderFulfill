package net.k1ra.orderfulfill.providers.chitchats.model

import com.google.gson.annotations.SerializedName

data class Rate(
    @SerializedName("postage_type")
    val postageType: String,
    @SerializedName("postage_carrier_type")
    val postageCarrierType: String,
    @SerializedName("postage_description")
    val postageDescription: String,
    @SerializedName("delivery_time_description")
    val deliveryTimeDescription: String,
    @SerializedName("tracking_type_description")
    val trackingTypeDescription: String,
    @SerializedName("is_insured")
    val isInsured: Boolean,
    @SerializedName("payment_amount")
    val paymentAmount: String
)