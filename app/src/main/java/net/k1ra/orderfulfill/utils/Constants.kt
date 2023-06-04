package net.k1ra.orderfulfill.utils

class Constants {
    companion object {
        const val apiKeyDbName = "OrderFulfillApiKeyDatabase"
        const val packagesDbName = "PackageDatabase"
        const val packageDescriptionsDbName = "PackageDesctiptionsDatabase"
        const val printersDbName = "PrinterDatabase"
        const val apiKeyEncryptionKeyName = "OrderFulfillApiKeyEncryptionKey"
        const val intentExtraPlatform = "Platform"
        const val intentExtraOAuthResponse = "AuthResponse"
        const val ebayOAuthScopes = "https%3A%2F%2Fapi.ebay.com%2Foauth%2Fapi_scope%2Fsell.fulfillment%20https%3A%2F%2Fapi.ebay.com%2Foauth%2Fapi_scope"
        const val eBayPendingOrderFilter = "orderfulfillmentstatus:%7BNOT_STARTED%7CIN_PROGRESS%7D "
    }
}