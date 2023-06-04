package net.k1ra.orderfulfill.providers.ebay.model

data class Order(
    val buyer: Buyer,
    val buyerCheckoutNotes: String,
    val creationDate: String,
    val fulfillmentStartInstructions: List<FulfillmentStartInstruction>,
    val lineItems: List<LineItem>,
    val orderFulfillmentStatus: String,
    val orderId: String,
    val orderPaymentStatus: String,
    val pricingSummary: PricingSummary
)