package net.k1ra.orderfulfill.providers.ebay.model

data class LineItem(
    val lineItemCost: Amount,
    val lineItemFulfillmentStatus: String,
    val lineItemId: String,
    val quantity: Int,
    val title: String,
    val legacyItemId: String,
    val legacyVariationId: String?,
    val variationAspects: List<NameValuePair>?
)