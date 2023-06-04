package net.k1ra.orderfulfill.platforms

import net.k1ra.orderfulfill.model.Platform
import net.k1ra.orderfulfill.platforms.ecommerce.EbayActions
import net.k1ra.orderfulfill.platforms.ecommerce.EcomPlatformActions
import net.k1ra.orderfulfill.platforms.ecommerce.ShopifyActions
import net.k1ra.orderfulfill.platforms.shipping.ChitChatsActions
import net.k1ra.orderfulfill.platforms.shipping.LettermailActions
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions
import java.lang.IllegalArgumentException

class PlatformProvider {
    companion object {
        //Have single instance of PlatformActions in order to maintain oAuth state variable
        private val ebayActions = EbayActions()
        private val shopifyActions = ShopifyActions()
        private val chitChatsActions = ChitChatsActions()
        private val lettermailActions = LettermailActions()

        fun forEcomType(platform: Platform) : EcomPlatformActions {
            return when (platform) {
                Platform.EBAY -> ebayActions
                Platform.SHOPIFY -> shopifyActions
                else -> throw IllegalArgumentException("Not an ecommerce platform")
            }
        }

        fun getShippingTypes() : ArrayList<ShippingPlatformActions> {
            return arrayListOf(chitChatsActions, lettermailActions)
        }

        fun getEcomTypes() : ArrayList<EcomPlatformActions> {
            return arrayListOf(ebayActions, shopifyActions)
        }
    }
}