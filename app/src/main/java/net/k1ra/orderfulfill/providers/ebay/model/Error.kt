package net.k1ra.orderfulfill.providers.ebay.model

data class Error(
    val errorId: Int,
    val domain: String,
    val category: String,
    val message: String,
    val longMessage: String
)