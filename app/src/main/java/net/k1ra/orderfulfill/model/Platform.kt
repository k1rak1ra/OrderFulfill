package net.k1ra.orderfulfill.model

enum class Platform {
    EBAY, SHOPIFY, CHIT_CHATS, LETTERMAIL;

    override fun toString(): String {
        return when (this) {
            EBAY -> "eBay"
            SHOPIFY -> "Shopify"
            CHIT_CHATS -> "Chit Chats"
            LETTERMAIL -> "Lettermail"
        }
    }
}